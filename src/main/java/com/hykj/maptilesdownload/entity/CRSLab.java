package com.hykj.maptilesdownload.entity;

import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.util.ProgressListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongProject:Maptilesdownload
 * @BelongPackage:com.hykj.maptilesdownload.entity
 * @Author:Administrator
 * @CreateTime:2021-11-11-15-28
 * @Description:cc
 */
public class CRSLab {
    private File sourceFile; //定义一个数据源（shp）
    private SimpleFeatureSource featureSource;
    private MapContent map;//显示地图

    public static void main(String[] args) throws Exception {
        CRSLab crsLab = new CRSLab();
        crsLab.displayShapefile();//调用显示shp的方法
    }

    private void displayShapefile() throws Exception{
        //新建窗口选择shp
        sourceFile = JFileDataStoreChooser.showOpenFile("shp",null);
        //检查合法性
        if (sourceFile == null){
            return;
        }
        //新建一个datastore把文件store化
        FileDataStore store = FileDataStoreFinder.getDataStore(sourceFile);
        //把featuresource读出来
        featureSource = store.getFeatureSource();
        //处理一下地图样式问题根据feature的类型新建style和layer添加到地图控件上
        map = new MapContent();
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource,style);
        map.layers().add(layer);
        //把控制Bar打开
        JMapFrame mapFrame = new JMapFrame(map);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);
        //在Bar上新建一个空白区放两个Button一个用来检查selfintersection和封闭问题，一个用来导出
        JToolBar toolBar = new JToolBar();
        toolBar.addSeparator();
        toolBar.add(new JButton(new ValidateGeometryAction()));
        toolBar.add(new JButton(new ExportShapefileAction()));

        mapFrame.setSize(800, 600);
        mapFrame.setVisible(true);
    }
    //一个geotools给的动作类
    class ValidateGeometryAction extends SafeAction{
        ValidateGeometryAction(){
            super("Validate geometry");
            putValue(Action.SHORT_DESCRIPTION,"check each geometry");
        }
        //定义抽象类方法
        public void action(ActionEvent e) throws Throwable{
            int numInvalid = validateFeatureGeometry(null);
            String msg;
            if (numInvalid == 0){
                msg = "Allfeature geometries is valid";
            }else {
                msg = "invalid geometries:"+numInvalid;
            }
            JOptionPane.showConfirmDialog(null,msg,"Geometry results", JOptionPane.YES_NO_CANCEL_OPTION);
        }
        private int validateFeatureGeometry(ProgressListener progress) throws Exception {
            final SimpleFeatureCollection featureCollection = featureSource.getFeatures();
            class VaildationVisitor implements FeatureVisitor {
                public  int numInvalidGeometries = 0;
                public void visit(Feature f){
                    SimpleFeature feature = (SimpleFeature) f;
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    if (geom!=null&&!geom.isValid()){
                        numInvalidGeometries++;
                        System.out.println("Invalid Geometry:"+feature.getID());
                    }
                }
            }
            VaildationVisitor visitor = new VaildationVisitor();

            featureCollection.accepts(visitor,progress);
            return visitor.numInvalidGeometries;
        }
    }
    class ExportShapefileAction extends SafeAction {
        ExportShapefileAction() {
            super("Export...");
            putValue(Action.SHORT_DESCRIPTION, "Export using current crs");
        }

        public void action(ActionEvent e) throws Throwable {
            exportToShapefile();
        }
    }
    private void exportToShapefile() throws Exception {
        SimpleFeatureType schema = featureSource.getSchema();
        JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
        chooser.setDialogTitle("Save reprojected shapefile");
        chooser.setSaveFile(sourceFile);
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.equals(sourceFile)) {
            JOptionPane.showMessageDialog(null, "Cannot replace " + file);
            return;
        }
        CoordinateReferenceSystem dataCRS = schema.getCoordinateReferenceSystem();
        //test
        CoordinateReferenceSystem worldCRS = map.getCoordinateReferenceSystem();
        MathTransform transform = CRS.findMathTransform(dataCRS, worldCRS, true);
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
        Map<String, Serializable> create = new HashMap<>();
        create.put("url", file.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);
        DataStore dataStore = factory.createNewDataStore(create);
        SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(schema, worldCRS);
        dataStore.createSchema(featureType);

        // Get the name of the new Shapefile, which will be used to open the FeatureWriter
        String createdName = dataStore.getTypeNames()[0];
        Transaction transaction = new DefaultTransaction("Reproject");
        try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                     dataStore.getFeatureWriterAppend(createdName, transaction);
             SimpleFeatureIterator iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                // copy the contents of each feature and transform the geometry
                SimpleFeature feature = iterator.next();
                SimpleFeature copy = writer.next();
                copy.setAttributes(feature.getAttributes());

                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                Geometry geometry2 = JTS.transform(geometry, transform);

                copy.setDefaultGeometry(geometry2);
                writer.write();
            }
            transaction.commit();
            JOptionPane.showMessageDialog(null, "Export to shapefile complete");
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
            JOptionPane.showMessageDialog(null, "Export to shapefile failed");
        } finally {
            transaction.close();
        }
    }
}
