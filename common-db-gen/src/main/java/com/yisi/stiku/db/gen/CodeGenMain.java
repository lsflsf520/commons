package com.yisi.stiku.db.gen;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mybatis.generator.api.ShellRunner;
import org.springframework.util.StringUtils;

import com.yisi.stiku.conf.BaseConfig;

/**
 * // * @see http://reform.mop.com/wiki/pages/viewpage.action?pageId=13239200
 * Created by Sean on 14-2-25.
 */
public class CodeGenMain {

	public static void main(String args[]) {
		String config = CodeGenMain.class.getResource("/gen.xml").getPath();
		String[] arg = { "-configfile", config, "-overwrite" };
		ShellRunner.main(arg);

		renameMapper2Dao(); //将生成的Mapper类的名字修改成Dao结尾
		
		move2TargetProject();
	}
	
	private static void move2TargetProject(){
		String targetProjDir = BaseConfig.getValue("target.project.dir");
		if(StringUtils.isEmpty(targetProjDir)){
			System.out.println("target.project.dir has not defined in application.properties, move work has abandoned");
			return;
		}
		
		String genCodeDir = System.getProperty("user.dir") + "/"
				+ BaseConfig.getValue("source.file.path");
		
		String javaPkgRootPath = BaseConfig.getValue("java.package.rootpath");
		
		File srcFileDir = new File(genCodeDir);
		
		if(!srcFileDir.exists() || srcFileDir.listFiles().length <= 0){
			System.out.println("no file exists in dir '" + srcFileDir.getAbsolutePath());
			return;
		}
		
		try {
			File targetJavaDir = new File(targetProjDir + "/src/main/java");
			FileUtils.copyDirectory(srcFileDir, targetJavaDir);
//			FileUtils.moveDirectory(srcFileDir, new File(targetProjDir + "/src"));
			System.out.println("source code has copied to '" + targetProjDir + "/src/main/java'");
			
			File mapperDir = new File(targetJavaDir.getAbsolutePath() + "/" + javaPkgRootPath.replaceAll("\\.", "/") + "/mapper");
			if(mapperDir.exists()){
				File targetMapperDir = new File(targetProjDir + "/src/main/resources/mapper");
				if(!targetMapperDir.exists()){
					targetMapperDir.mkdirs();
				}
				FileUtils.copyDirectory(mapperDir, targetMapperDir);
//				FileUtils.moveDirectory(mapperDir, new File(targetProjDir + "/src/main/resources"));
				System.out.println("mapper xml code dir has copied to '" + targetMapperDir.getAbsolutePath() +"'");
				
				FileUtils.deleteDirectory(mapperDir); // src/main/java中的mapper文件夹没用了，可以删掉
			}
			
			FileUtils.deleteDirectory(new File(srcFileDir.getAbsolutePath() + "/" + javaPkgRootPath.split("\\.")[0])); // 生成代码的源目录也不需要了，可以删掉
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void renameMapper2Dao() {
		String genCodeDir = System.getProperty("user.dir") + "/"
				+ BaseConfig.getValue("source.file.path");
		File genFileDir = new File(genCodeDir);
		if(!genFileDir.exists()){
			System.out.println("'" + genCodeDir + "' not exists." );
			return ;
		}
		Collection<File> daoFiles = FileUtils.listFiles(genFileDir,
				new String[] { "java" }, true);
		for (File file : daoFiles) {
			String filePath = file.getAbsolutePath();
			if (filePath.endsWith("Mapper.java")) {
				String newFilePath = filePath
						.replace("Mapper.java", "Dao.java");
				File srcFile = new File(filePath);
				try {
					String content = FileUtils.readFileToString(srcFile);
					String baseName = FilenameUtils.getBaseName(filePath);
					content = content.replaceAll(baseName,
							baseName.replace("Mapper", "Dao"));
					FileUtils.write(new File(newFilePath), content, "UTF-8");

					srcFile.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// srcFile.renameTo(new File(newFilePath));
			}
		}
	}
}
