package org.astri.spitfire.adapter;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/07/12
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Algorithm {

    private String name;
    private int index;
    private int intensify;

    public Algorithm(String name){
        this.name = name;
    }

    public Algorithm(String name, int index, int intensify) {
        this.name = name;
        this.index = index;
        this.intensify = intensify;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIntensify() {
        return intensify;
    }

    public void setIntensify(int intensify) {
        this.intensify = intensify;
    }

    /**
     * 生成写入设备的参数
     * 例如：0302
     * 03 代表 第4个算法
     * 03 代表 强度为 4
     * @return
     */
    public String genAlgSettingPara(){
        return "0"+this.index+"0"+ this.intensify;
    }

    @Override
    public String toString() {
        return "Algorithm{" +
                "name='" + name + '\'' +
                ", index=" + index +
                ", intensify=" + intensify +
                '}';
    }
}
