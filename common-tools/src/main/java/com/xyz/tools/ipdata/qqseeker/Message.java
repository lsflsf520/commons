package com.xyz.tools.ipdata.qqseeker;

import com.xyz.tools.common.utils.BaseConfig;

public interface Message {  
    String bad_ip_file="IP地址库文件错误";  
    String unknown_country="未知国家";  
    String unknown_area="未知地区";  
    String bad_ip_addr="不合法的ip地址";
    
    String DATA_BASE_DIR = BaseConfig.getValue("ipdata.dir", "/data/www/static/files/ipdata");
}  
