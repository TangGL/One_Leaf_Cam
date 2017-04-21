package com.example.ray.finalex;

import java.util.List;

/**
 * Created by 80518 on 2016/12/13.
 */

public class JavaBean {
    /**
     * code : 10000
     * charge : false
     * msg : 查询成功
     * result : [{"DistrictName":"中国","ID":"8d46626c61587c5ac57326db48f1d61d","TypeName":"国家"},{"DistrictName":"北京市","ID":"bb33e0e0545c9ae7db043d499b59b518","TypeName":"市"},{"DistrictName":"朝阳区","ID":"b11e7c26cf082d3b34908c071f7a2e65","TypeName":"区县"},{"DistrictName":"国家体育场","ID":"4597ce91c5eb32b952ab87b24b802ba4","TypeName":"体育场馆"}]
     */

    private String code;
    private boolean charge;
    private String msg;
    private List<ResultBean> result;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isCharge() {
        return charge;
    }

    public void setCharge(boolean charge) {
        this.charge = charge;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * DistrictName : 中国
         * ID : 8d46626c61587c5ac57326db48f1d61d
         * TypeName : 国家
         */

        private String DistrictName;
        private String ID;
        private String TypeName;

        public String getDistrictName() {
            return DistrictName;
        }

        public void setDistrictName(String DistrictName) {
            this.DistrictName = DistrictName;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getTypeName() {
            return TypeName;
        }

        public void setTypeName(String TypeName) {
            this.TypeName = TypeName;
        }
    }
}
