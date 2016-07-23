package com.example.youngwind.mdp_android.model;


/**
 * Created by youngwind on 16/7/23.
 */
public class ComponentVersionList {

    // 状态码
    public int code;
    public componentVersion[] componentVersions;

    public class componentVersion {
        //组件包id
        public int component_id;

        // 组件包名称
        public String name;

        // 组件包最新版本
        public String component_version;

        // 组件包最后更新时间
        public String updatedAt;
    }


}
