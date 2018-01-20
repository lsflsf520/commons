package com.ujigu.secure.upfile.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.secure.common.utils.ImageUtils;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.upfile.bean.ImgErrorCode;
import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.upfile.service.ProjUploadService;
import com.ujigu.secure.upfile.util.PathUtils;
import com.ujigu.secure.web.util.WebUtils;

@Controller
@RequestMapping("file")
public class FileController {
	
	private final static String REG_FILE_MODULES = "upfile.modules";
	
	@RequestMapping(value="base64/upload")
	@ResponseBody
	public ResultModel base64upload(HttpServletResponse response, String prefix, String module, String base64Code, String filename){
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");
        
        if(StringUtils.isNotBlank(prefix) && !PathUtils.isRightDir(prefix)){
			LogUtils.warn("not support module %s, prefix %s", module, prefix);
			return new ResultModel("ILLEGAL_PREFIX", "目录前缀不合法");
		}
        
		if (!isRegModule(module)) {
			LogUtils.warn("not support module %s in white modules config %s", module, REG_FILE_MODULES);
			return new ResultModel(ImgErrorCode.NO_SUCH_MODULE.name(), ImgErrorCode.NO_SUCH_MODULE.getMsg());
		}
		if(StringUtils.isBlank(base64Code)){
			LogUtils.warn("not found any file element in the form for module %s, base64Code %s", module, base64Code);
			return new ResultModel(ImgErrorCode.NO_FILE_ELEM.name(), ImgErrorCode.NO_FILE_ELEM.getMsg());
		}
		
		String suffix = FilenameUtils.getExtension(filename);
		if(!isWhiteSuffix(suffix)){
			LogUtils.warn("not support suffix %s", suffix);
			return new ResultModel("ILLEGAL_SUFFIX", "不支持的文件类型");
		}
		
		String localFilePath = getLocalFilePath(filename, prefix, module);
		
		PathUtils.checkDirs(FilenameUtils.getFullPathNoEndSeparator(localFilePath)); // 检查目录是否存在,若不存在，则创建之
		
		byte[] bytes = Base64.decodeBase64(base64Code);
		try {
			File targetFile = new File(localFilePath);
			FileUtils.writeByteArrayToFile(targetFile, bytes);
			
			RetFileInfo imgInfo = new RetFileInfo();
			imgInfo.setAccessDomain(ProjUploadService.getAccessDomain());
			String accessUri = localFilePath.replace(ProjUploadService.getBaseStoreDir(), "");
			imgInfo.setAccessUri(accessUri);
			imgInfo.getAccessUrl();  //确保accessUrl赋值成功，因为Gson只对有值的属性才生效
			imgInfo.getExtension();  //确保extension赋值成功，因为Gson只对有值的属性才生效
			imgInfo.getHumanSize();
			imgInfo.setOriginName(filename);
			
			ResultModel resultModel = new ResultModel(imgInfo);
			return resultModel;
		} catch (IOException e) {
			LogUtils.error("file:%s, base64Code:%s", e, localFilePath, base64Code);
		}
		
		return new ResultModel("SAVE_ERR", "存储文件出错，请稍后重试！");
	}
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public ResultModel uploadfile(MultipartHttpServletRequest request,
			HttpServletResponse response, String prefix, String module) throws IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");

//		String module = request.getParameter("module");
		String clientIP = WebUtils.getIpAddr(request);
		String base64 = request.getParameter("base64");
		Map<String, MultipartFile> fileMap = request.getFileMap();
		if(CollectionUtils.isEmpty(fileMap)){
			LogUtils.warn("not found any file element in the form for module %s", module);
			return new ResultModel(ImgErrorCode.NO_FILE_ELEM.name(), ImgErrorCode.NO_FILE_ELEM.getMsg());
		}

		MultipartFile partFile = fileMap.entrySet().iterator().next().getValue();
		
		return uploadFile(partFile, prefix, module, clientIP, base64);
	}
	
	@RequestMapping(value = "multi/upload", method = RequestMethod.POST)
	@ResponseBody
	public ResultModel multiUpload(MultipartHttpServletRequest request,
			HttpServletResponse response, String prefix, String module){
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");
//        String module = request.getParameter("module");
        String clientIP = WebUtils.getIpAddr(request);
		String base64 = request.getParameter("base64");
		Map<String, MultipartFile> fileMap = request.getFileMap();
		if(CollectionUtils.isEmpty(fileMap)){
			LogUtils.warn("not found any file element in the form for module %s", module);
			return new ResultModel(ImgErrorCode.NO_FILE_ELEM.name(), ImgErrorCode.NO_FILE_ELEM.getMsg());
		}
		
		List<Object> models = new ArrayList<>();
		for(MultipartFile partFile : fileMap.values()){
			ResultModel resultModel = uploadFile(partFile, prefix, module, clientIP, base64);
			if(resultModel.getModel() instanceof RetFileInfo){
				models.add(resultModel.getModel());
			}else{
				LogUtils.warn("error upload file %s", partFile.getOriginalFilename());
			}
		}
		
		return new ResultModel(models);
	}
	
	private boolean isWhiteSuffix(String suffix){
		if(StringUtils.isBlank(suffix)){
			return false;
		}
		suffix = suffix.trim();
        String[] suffixes = BaseConfig.getValueArr("upfile.white.suffix");
		
		return suffixes != null && Arrays.asList(suffixes).contains(suffix);
	}
	
	private boolean isRegModule(String module){
		String[] regProjs = BaseConfig.getValueArr(REG_FILE_MODULES);
		
		return regProjs != null && Arrays.asList(regProjs).contains(module);
	}

	private ResultModel uploadFile(MultipartFile partFile, String prefix, String module, String clientIP, String base64){
		if(partFile == null || partFile.getSize() <= 0){
			LogUtils.warn("not found any file element in the form for module %s", module);
			return new ResultModel(ImgErrorCode.NO_FILE_ELEM.name(), ImgErrorCode.NO_FILE_ELEM.getMsg());
		}
		
		if(StringUtils.isNotBlank(prefix) && !PathUtils.isRightDir(prefix)){
			LogUtils.warn("not support module %s, prefix %s", module, prefix);
			return new ResultModel("ILLEGAL_PREFIX", "目录前缀不合法");
		}
		
		if (!isRegModule(module) 
				&& !ImageController.isRegModule(module)  //此处是为了兼容以前的版本
				) {
			LogUtils.warn("not support module %s in white modules config %s", module, REG_FILE_MODULES);
			return new ResultModel(ImgErrorCode.NO_SUCH_MODULE.name(), ImgErrorCode.NO_SUCH_MODULE.getMsg());
		}
		

		String originName = partFile.getOriginalFilename();
		String suffix = FilenameUtils.getExtension(originName);
		if(!isWhiteSuffix(suffix)){
			LogUtils.warn("not support suffix %s, filename:%s", suffix, originName);
			return new ResultModel("ILLEGAL_SUFFIX", "不支持的文件类型");
		}
		
		String localFilePath = getLocalFilePath(originName, prefix,
				module);

		PathUtils.checkDirs(FilenameUtils.getFullPathNoEndSeparator(localFilePath)); // 检查目录是否存在,若不存在，则创建之

		ImgErrorCode errorCode = saveFileFromInputStream(partFile, localFilePath,
				clientIP);

		if (ImgErrorCode.SUCCESS.equals(errorCode)) {
			RetFileInfo imgInfo = new RetFileInfo();
			imgInfo.setAccessDomain(ProjUploadService.getAccessDomain());
			String accessUri = localFilePath.replace(ProjUploadService.getBaseStoreDir(), "");
			imgInfo.setAccessUri(accessUri);
			imgInfo.getAccessUrl();  //确保accessUrl赋值成功，因为Gson只对有值的属性才生效
			imgInfo.getExtension();  //确保extension赋值成功，因为Gson只对有值的属性才生效
			imgInfo.getHumanSize();
			imgInfo.setOriginName(originName);
			
			ResultModel resultModel = new ResultModel(imgInfo);
			
			if("true".equals(base64)){
				String base64Code = ImageUtils.getImageStr(localFilePath);
				
				imgInfo.setBase64Code(base64Code);
			}
			
			return resultModel;
		}
		
		return new ResultModel(errorCode.name(), errorCode.getMsg());
	}
	
	private String getLocalFilePath(String filename, String prefix, String module) {
		String suffix = StringUtils.isBlank(filename) ? "" : FilenameUtils.getExtension(filename);

		Date currDate = new Date();
		String generationfileName = DateUtil.formatDate(currDate,
				"yyyyMMddHHmmss")
				+ new Random(System.currentTimeMillis()).nextInt(1000);

		String newFileName = generationfileName + (StringUtils.isNotBlank(suffix) ? "." + suffix : "");
		return ProjUploadService.getBaseStoreDir() + File.separator + (StringUtils.isNotBlank(prefix) ? prefix.trim() + File.separator : "") + module
				+ File.separator + DateUtil.getMonthStr(currDate)
				+ File.separator + currDate.getTime() % 1500 + File.separator
				+ newFileName;
	}
	
	private ImgErrorCode saveFileFromInputStream(MultipartFile partFile,
			String filePath, String clientIP) {

		ImgErrorCode errorCode = ImgErrorCode.SUCCESS;
		FileOutputStream fs = null;
		InputStream stream = null;
		try {
			stream = partFile.getInputStream();
			byte[] fileBytes = IOUtils.toByteArray(stream);
			fs = new FileOutputStream(filePath);
			fs.write(fileBytes);

			LogUtils.debug("file '" + filePath
						+ "' has uploaded success, total bytes "
						+ fileBytes.length);

				// resultObj.addProperty("filePath", filePath);
				// resultObj.addProperty("bytes", fileBytes.length); //
				// 改图片的字节数，以byte为单位
				// resultObj.addProperty("friendlySize",
				// FileUtils.byteCountToDisplaySize(fileBytes.length)); //
				// 改图片文件的友好大小。如果大于1M，则以M为单位；如果大于1K并小于1M，则以K为单位；小于1K，则用以byte为单位
		} catch (IOException e) {
			LogUtils.error("path:%s, clientIP:%s", e,  filePath, clientIP);
			errorCode = ImgErrorCode.SAVE_IO_EXCEPTION;
		} finally {
			if (fs != null) {
				try {
					fs.flush();
				} catch (IOException e) {
					LogUtils.error("path:%s, clientIP:%s", e,  filePath, clientIP);
				}
			}
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					LogUtils.error("path:%s, clientIP:%s", e,  filePath, clientIP);
				}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					LogUtils.error("path:%s, clientIP:%s", e,  filePath, clientIP);
				}
			}

		}

		return errorCode;
	}
}
