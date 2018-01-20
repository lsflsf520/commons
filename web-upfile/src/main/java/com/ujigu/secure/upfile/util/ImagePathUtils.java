package com.ujigu.secure.upfile.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.utils.BaseConfig;

public class ImagePathUtils {
	private final static Logger LOG = LoggerFactory
			.getLogger(ImagePathUtils.class);

	/**
	 * 清理url前端缓存，以及url对应的缩略图的前端缓存
	 */
	public static void purge(String url) throws IOException {
		
		if (StringUtils.isNotBlank(url)) {
			execPurge(url);
			
			Set<String> urls = getPaths(url);
			if (null != urls) {
				for (String s : urls) {
					execPurge(s);
				}
			}

		}
	}

	private static void execPurge(String url) throws IOException {
		Runtime.getRuntime().exec(new String[] { "sh", "-c", "__sh purge " + url });
	}

	private static Set<String> getPaths(String url) throws IOException {
		Set<String> urlSet = new HashSet<String>();

		if (StringUtils.isNotBlank(url)) {
			String lastName = FilenameUtils.getExtension(url);
			if (StringUtils.isNotBlank(lastName)) {
				String projectName = getProjectName(url);
				if (StringUtils.isNotBlank(projectName)) {
					List<String> sizes = getSizes(projectName);
					if (null != sizes) {
						for (String size : sizes) {
							if (!StringUtils.isBlank(size)) {
								urlSet.add(url + size + "." + lastName);
							}
						}
					}
				}
			}

		}
		return urlSet;
	}

	private static List<String> getSizes(String projectName) {
		List<String> sizeList =  new ArrayList<String>(Arrays.asList(BaseConfig.getValueArr(projectName + ".sizeRule")));
		fillSizeList(projectName, sizeList,".showSize");
		fillSizeList(projectName, sizeList,".special.sizeRule");
		return sizeList;
	}

	private static void fillSizeList(String projectName, List<String> sizeList,String sizeStr) {
		String[] sizes = null;
		try{
			 sizes = BaseConfig.getValueArr(projectName + sizeStr);
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		if(null!=sizes){
			sizeList.addAll(new ArrayList<String>(Arrays.asList(sizes)));
		}
	}

	private static String getProjectName(String url) throws IOException {
		if (StringUtils.isNotBlank(url)) {
			String path = new URL(url).getPath();
			if (StringUtils.isNotBlank(path) && path.length() > 5) {
				path = path.substring(5);
				int index = path.indexOf("/");
				if (-1 != index) {
					return path.substring(0, index);
				}
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		String[] s = {"1"};
		String[] s2 = {"2"};
		
		List<String> l = new ArrayList<String>(Arrays.asList(s));
		
		l.addAll(new ArrayList<String>(Arrays.asList(s2)));
		
		System.out.println(l.toString());
		
		//		String url = "http://i1.mopimg.cn/img/dzh/2014-08/131/20140820153513145.jpg";
//
//		for (String s : getPaths(url)) {
//			System.out.println(s);
//		}
	}

}

// private static List<String> sizeRules = new CopyOnWriteArrayList<String>();
// static {
// reloadSizeRules();
// }
//
// private static void watchNode(String path) {
// ZKClient.get().watchStrValueNode(path, new StringValueWatcher() {
// @Override
// public void valueChanged(String l) {
// reloadSizeRules();
// }
// });
// }
//
// private static void reloadSizeRules() {
//
// sizeRules.clear();
// String[] projects =
// ConfigOnZk.getInstance().getArrayValue(ImgConstant.ZK_SYSTEM_PATH,
// "mop.upimg.projects");
// if (null != projects) {
// for (String projectName : projects) {
// String[] sizes =
// ConfigOnZk.getInstance().getArrayValue(ImgConstant.ZK_SYSTEM_PATH,
// projectName + ".sizeRule");
// if (null != sizes) {
// for (String size : sizes) {
// if (!StringUtils.isBlank(size)) {
// sizeRules.add(size);
// }
// }
// }
// }
// }
// }
