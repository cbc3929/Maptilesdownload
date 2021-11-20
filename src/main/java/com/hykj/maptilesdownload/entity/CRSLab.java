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

    private void displayShapefile() throws Exception {
        //新建窗口选择shp
        sourceFile = JFileDataStoreChooser.showOpenFile("shp", null);
        //检查合法性
        if (sourceFile == null) {
            return;
        }
        //新建一个datastore把文件store化
        FileDataStore store = FileDataStoreFinder.getDataStore(sourceFile);
        //把featuresource读出来
        featureSource = store.getFeatureSource();
        //处理一下地图样式问题根据feature的类型新建style和layer添加到地图控件上
        map = new MapContent();
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
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
    class ValidateGeometryAction extends SafeAction {
        ValidateGeometryAction() {
            super("Validate geometry");
            putValue(Action.SHORT_DESCRIPTION, "check each geometry");
        }

        //定义抽象类方法
        public void action(ActionEvent e) throws Throwable {
            int numInvalid = validateFeatureGeometry();
            String msg;
            if (numInvalid == 0) {
                msg = "Allfeature geometries is valid";
            } else {
                msg = "invalid geometries:" + numInvalid;
            }
            JOptionPane.showConfirmDialog(null, msg, "Geometry results", JOptionPane.YES_NO_CANCEL_OPTION);
        }

        private int validateFeatureGeometry() throws Exception {
            final SimpleFeatureCollection featureCollection = featureSource.getFeatures();
            class VaildationVisitor implements FeatureVisitor {
                public int numInvalidGeometries = 0;

                public void visit(Feature f) {
                    SimpleFeature feature = (SimpleFeature) f;
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    if (geom != null && !geom.isValid()) {
                        numInvalidGeometries++;
                        System.out.println("Invalid Geometry:" + feature.getID());
                    }
                }
            }
            VaildationVisitor visitor = new VaildationVisitor();

            featureCollection.accepts(visitor, null);
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
    /*婉儿解析（详）
一、技能配合及连招
1.	技能（21）（13）（23）可回血并加强普攻伤害，
 21的伤害在末端会爆炸一下，并超过2技能的最远距离！
 2技能收割发现没有收割只差一点点血，立马在2轨迹放1会有多段伤害！
2.	常用技能连招233133、133233、21333、233313、2133（来回速飞）、随机多变性连招、Z字抖动
细节拆分：2前3后3或2前3前3后3（防止2技能轨迹中伤害丢失未打满）
3.	刹车：23 刹车，2技能放出接大3一下瞬间松开左手（左手指离开屏幕）
4.	拐笔
拐笔在233基础上，原先左轮盘手指朝前方飞两端改变成往前飞一段再往旁边飞一段

5.	藏笔

6.	双笔2332133、213333、2331323|
7.	无大平a流婉儿：aa2a1a2
二、团战思路及进场时机
伤害刮痧分析：21双笔打空、飞天途中不睬人、装备核心搞错（鞋子+四件大法师装备伤害效果=65%/鞋子+五件法师装备效果85% /六法装自保能力85%伤害效果90%）
逆风反打：队友较坑容易被抓单形成4v5局面/打团残局形成4v5或2v4残局
要点：保证中路或其他路兵线不进塔，213快速清线
如2v4看懂对面站位，22人站位13人站位都可以速飞秒
顺风打法：探草+遁草（遁草前2技能要探草配合回响），2技能点头起步秒，大招回撤收割，利用这点让对方永远没节奏并压塔
关于压塔推塔，在前者使对面少人的基础上适当卖，主动233 起步动作2133打一波伤害刹车卡一段大招骗技能在起飞，其次阵容很肉也是会刮痧哒，所以要用21消耗！切记不能直接飞！
盾山：举盾时可绕开千万不能靠近！盾山举盾进度条快消失马上瞬笔（速飞2133回头断大流）消耗
张良：藏笔或者原地卡三至四段大招闪现3
东皇：反应大招飞天吃大招，靠反应和对距离的把握，难度大。 藏笔或者靠队友卖，其次也可靠蹲草丛切后排。
公孙离：藏笔或原地飞或者等他2技能交了在上
三、布局设置及操作设置
攻击方式：选择最近单位
摇杆：无固定。
技能释放：三项第一，灵敏度最高。
自定义布局：根据个人手感调整轮盘大小。
四、节奏意识
2级边路帮忙（偷吃经济）速回中路清线并配合打野拿下河蟹，如果没有成功或者途中死亡，等待四级还是能单杀！其他抓人call细讲。
五、各中单对线
Call细讲或文字QQ回答


    * */
}
