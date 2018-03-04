package com.xyz.tools.common.utils;

import java.util.ArrayList;

/**
** @author Administrator
** @version 2017年9月14日上午11:14:04
** @Description
*/
public class DikaerjiUtil {
	
	/**
	 * @param al0
	 * @return
	 * @Decription 对多个list进行排列组合
	 * @Author Administrator
	 * @Time 2017年9月14日上午11:15:37
	 * @Exception
	 */
	public static ArrayList combineList(ArrayList al0){
		ArrayList a0 = (ArrayList) al0.get(0);// l1  
        ArrayList result = new ArrayList();// 组合的结果  
        for (int i = 1; i < al0.size(); i++) {  
            ArrayList a1 = (ArrayList) al0.get(i);  
            ArrayList temp = new ArrayList();  
            // 每次先计算两个集合的笛卡尔积，然后用其结果再与下一个计算  
            for (int j = 0; j < a0.size(); j++) {  
                for (int k = 0; k < a1.size(); k++) {  
                    ArrayList cut = new ArrayList();  
  
                    if (a0.get(j) instanceof ArrayList) {  
                        cut.addAll((ArrayList) a0.get(j));  
                    } else {  
                        cut.add(a0.get(j));  
                    }  
                    if (a1.get(k) instanceof ArrayList) {  
                        cut.addAll((ArrayList) a1.get(k));  
                    } else {  
                        cut.add(a1.get(k));  
                    }  
                    temp.add(cut);  
                }  
            }  
            a0 = temp;  
            if (i == al0.size() - 1) {  
                result = temp;  
            }  
        }  
        return result;  
	}

}
