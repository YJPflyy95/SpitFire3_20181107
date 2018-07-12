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
    private String index;
    private String intensify;

    public Algorithm(String name){
        this.name = name;
    }

    public Algorithm(String index, String intensify) {
        this.index = index;
        this.intensify = intensify;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIntensify() {
        return intensify;
    }

    public void setIntensify(String intensify) {
        this.intensify = intensify;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Algorithm{" +
                "name='" + name + '\'' +
                ", index='" + index + '\'' +
                ", intensify='" + intensify + '\'' +
                '}';
    }
}
