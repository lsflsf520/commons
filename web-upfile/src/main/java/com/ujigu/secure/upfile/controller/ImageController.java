package com.ujigu.secure.upfile.controller;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
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
import com.ujigu.secure.common.utils.BeanUtils;
import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.secure.common.utils.ImageUtils;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.upfile.bean.ImgConstant;
import com.ujigu.secure.upfile.bean.ImgErrorCode;
import com.ujigu.secure.upfile.bean.ImgPosition;
import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.upfile.service.ProjUploadService;
import com.ujigu.secure.upfile.service.RetInfoHandler;
import com.ujigu.secure.upfile.util.ImagePathUtils;
import com.ujigu.secure.upfile.util.ImageShellUtil;
import com.ujigu.secure.upfile.util.PathUtils;
import com.ujigu.secure.upfile.util.ThreadExecutor;
import com.ujigu.secure.web.util.WebUtils;

@Controller
@RequestMapping("image")
public class ImageController {
	private final static Map<String, ProjUploadService> imgHandlerMap = new HashMap<String, ProjUploadService>();
	private final static Map<String, RetInfoHandler> retHandlerMap = new HashMap<String, RetInfoHandler>();
	
	private final static String REG_PROJECTS_KEY = "upimg.modules";
	private final static Random rand = new Random(System.currentTimeMillis());
	
	private static String backupBasePath = "/data/backup/image";

	static {
		String[] projects = BaseConfig.getValueArr(REG_PROJECTS_KEY);
		if (projects != null && projects.length > 0) {
			for (String project : projects) {
				String handlerClz = BaseConfig.getValue(project
						+ ".handler");
				if (StringUtils.isBlank(handlerClz)) {
					handlerClz = "com.ujigu.secure.upfile.service.impl.CompressionUploadService";
				}

				try {
					imgHandlerMap.put(project, (ProjUploadService) Class
							.forName(handlerClz).newInstance());
				} catch (Exception e) {
					LogUtils.error("init hanlder for project '%s' with handler class '%s'", e, project, handlerClz);
				}
			}
		}
	}

//	@RequestMapping(value = "/to", method = RequestMethod.GET)
//	public String toUpload(HttpServletRequest request) {
//		request.setAttribute("uid", "2392300");
//		return "upload";
//	}
	
	/*@RequestMapping(value = "/getDelImgFile")
	public HttpServletResponse getDelImg(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String imgUrl = request.getParameter("imgUrl");
		String imgPath = this.getImgPath(imgUrl);
		imgPath = backupBasePath + imgPath;
		File imgFile = new File(imgPath);
		response.setContentType("application/x-download");
		
		InputStream is = new BufferedInputStream(new FileInputStream(imgFile));
		byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		
		response.reset();
		 response.addHeader("Content-Disposition", "attachment;filename=\""
		 +imgFile.getName() );
		response.addHeader("Content-Length", "" + new File(imgPath).length());
		OutputStream os = new BufferedOutputStream(response.getOutputStream());
		response.setContentType("application/octet-stream");
		os.write(buffer);
		os.flush();
		os.close();
		return response;
	}*/
	/*@RequestMapping(value = "/getMD5")
	public void getMD5(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");
        
    	String imgUrl = request.getParameter("imgUrl");
		String imgPath = this.getImgPath(imgUrl);
		
		if(StringUtils.isBlank(imgPath)){
			//路径不存在
			WebUtils.writeJsonByObj(-1, response, request);
			return;
		}
		
		File f = new File(imgPath);
		if (!f.isFile() || !f.exists()) {
			imgPath = backupBasePath + imgPath;
			f = new File(imgPath);
			if (!f.isFile() || !f.exists()) {
				//路径不存在
				WebUtils.writeJsonByObj(-1, response, request);
				return;
			}
		}
		String md5 = MD5Util.md5sum(imgPath);
		//路径不存在
		WebUtils.writeJsonByObj(md5, response, request);
		return;
	}*/

	@RequestMapping(value = "/recoverImg")
	@ResponseBody
	public ResultModel recoverImg(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");
        
		//String clientIP = WebUtils.getIpAddr(request);
		String imgUrl = request.getParameter("imgUrl");
		String imgPath = this.getImgPath(imgUrl);
		
		if(StringUtils.isBlank(imgPath)){
			//路径不存在
			return new ResultModel("NOT_EXIST", "图片路径不存在");
		}
		String backupPath = backupBasePath + imgPath;
		
		File dir = new File(imgPath).getParentFile();
		dir.mkdirs();
		
		File f = new File(backupPath);
		if ( f.exists()) {
			try {
				Runtime.getRuntime().exec(new String[]{"sh", "-c", "mv -f " + backupPath+"*" + " " + dir.getPath()});
				//Runtime.getRuntime().exec(new String[]{"sh", "-c", "__sh purge " + imgUrl});
				ImagePathUtils.purge(imgUrl);
			} catch (Exception e) {
				LogUtils.error("recover photo error errMsg=%s", e, e.getMessage());
				return new ResultModel("PURGE_ERR", "图片处理失败！");
			}
		}
		
		return new ResultModel(true);
		
	}
	
	@RequestMapping(value = "/delImg")
	@ResponseBody
	public ResultModel delImg(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");
		
		//String clientIP = WebUtils.getIpAddr(request);
		String imgUrl = request.getParameter("imgUrl");
		String imgPath = this.getImgPath(imgUrl);
		
		if(StringUtils.isBlank(imgPath)){
			//路径不存在
			return new ResultModel("NOT_EXIST", "图片路径不存在");
		}
		File imgFile = new File(imgPath);
		
		if (imgFile.exists() && imgFile.isFile()) {
			String backupPath = backupBasePath + imgPath;
			File backupDir = new File(backupPath).getParentFile();
			backupDir.mkdirs();
			
			
			try {
				Runtime.getRuntime().exec(new String[]{"sh", "-c", "mv -f " + imgPath+"*"+ " "+backupDir.getPath()});
				//Runtime.getRuntime().exec(new String[]{"sh", "-c", "__sh purge " + imgUrl});
				ImagePathUtils.purge(imgUrl);
			} catch (Exception e) {
				LogUtils.error("删除文件异常，url=%s", e, imgUrl);
				//异常
				return new ResultModel("PURGE_ERR", "图片处理失败！");
			}
		}
		
		return new ResultModel(true);
	}

	private String getImgPath(String imgUrl) throws MalformedURLException {
		String baseDir = ProjUploadService.getBaseStoreDir();
		return baseDir+new URL(imgUrl).getPath().substring(4);
	}
	
	@RequestMapping(value = "base64/upload", method = RequestMethod.POST)
	@ResponseBody
	public ResultModel uploadBase64(HttpServletRequest request, HttpServletResponse response, String prefix, String module, String base64Code, String suffix){
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");

        if(StringUtils.isNotBlank(prefix) && !PathUtils.isRightDir(prefix)){
			LogUtils.warn("not support module %s, prefix %s", module, prefix);
			return new ResultModel("ILLEGAL_PREFIX", "目录前缀不合法");
		}
        
        if (!isRegModule(module)) {
			LogUtils.warn("not support module %s in white modules config %s", module, REG_PROJECTS_KEY);
			return new ResultModel(ImgErrorCode.NO_SUCH_MODULE.name(), ImgErrorCode.NO_SUCH_MODULE.getMsg());
		}
        if(StringUtils.isBlank(base64Code)){
			LogUtils.warn("not found any file element in the form for module %s, base64Code %s", module, base64Code);
			return new ResultModel(ImgErrorCode.NO_FILE_ELEM.name(), ImgErrorCode.NO_FILE_ELEM.getMsg());
		}
        if (!isImgSuffix(suffix)) {
			suffix = "jpg";
		}
        
        String zkNewHandler = BaseConfig.getValue(module + ".handler");
		ProjUploadService uploadServ = null;
		try {
		    uploadServ = BeanUtils.getBean(imgHandlerMap, module, zkNewHandler);
		} catch (Exception e) {
			LogUtils.error("init hanlder for project '%s' with handler class '%s'", e, module, zkNewHandler);
		}

		if(uploadServ == null){
			
			return new ResultModel(ImgErrorCode.HANDLER_INSTANCE_ERROR.name(), ImgErrorCode.HANDLER_INSTANCE_ERROR.getMsg());
		}
        
        String localFilePath = getLocalFilePath(suffix, prefix, module);
		
		PathUtils.checkDirs(FilenameUtils.getFullPathNoEndSeparator(localFilePath)); // 检查目录是否存在,若不存在，则创建之
		
		byte[] bytes = Base64.decodeBase64(base64Code);
		try {
			File targetFile = new File(localFilePath);
			FileUtils.writeByteArrayToFile(targetFile, bytes);
			
			return postHandle(request, uploadServ, module, localFilePath, suffix);
		} catch (IOException e) {
			LogUtils.error("file:%s, base64Code:%s", e, localFilePath, base64Code);
		}
		
		return new ResultModel("SAVE_ERR", "存储文件出错，请稍后重试！");
		
	}
	
	private String getLocalFilePath(String suffix, String prefix, String module) {
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

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public ResultModel uploadImg(MultipartHttpServletRequest request,
			HttpServletResponse response, String prefix, String module) throws IOException {

		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");
        
        if(StringUtils.isNotBlank(prefix) && !PathUtils.isRightDir(prefix)){
			LogUtils.warn("not support module %s, prefix %s", module, prefix);
			return new ResultModel("ILLEGAL_PREFIX", "目录前缀不合法");
		}

//		String module = request.getParameter("module");
		// String domain = request.getServerName(); // 获取当前的域名
		ImgErrorCode errorCode = commonCheck(request, module);
		if (ImgErrorCode.SUCCESS.equals(errorCode)) {
			String clientIP = WebUtils.getIpAddr(request);

			Map<String, MultipartFile> fileMap = request.getFileMap();
			MultipartFile partFile = fileMap.entrySet().iterator().next().getValue();

			String zkNewHandler = BaseConfig.getValue(module + ".handler");
			ProjUploadService uploadServ = null;
			try {
			    uploadServ = BeanUtils.getBean(imgHandlerMap, module, zkNewHandler);
			} catch (Exception e) {
				LogUtils.error("init hanlder for project '%s' with handler class '%s'", e, module, zkNewHandler);
			}

			if(uploadServ == null){
				errorCode = ImgErrorCode.HANDLER_INSTANCE_ERROR;
				
				return new ResultModel(errorCode.name(), errorCode.getMsg());
			}

			String localFilePath = uploadServ.getLocalFilePath(partFile, prefix,
					module);

			PathUtils.checkDirs(FilenameUtils.getFullPathNoEndSeparator(localFilePath)); // 检查目录是否存在,若不存在，则创建之

			errorCode = saveFileFromInputStream(partFile, localFilePath,
					clientIP);

			if (ImgErrorCode.SUCCESS.equals(errorCode)) {
				//上传完成后，可以在这先对图片进行相关检查，比如是否为违禁图片
				//如果为违禁图片，需要调用 resultMap.put(ImgConstant.ERROR_CODE_KEY, ImgErrorCode.PROHIBITED_IMG); 设置一下返回的code，然后略过下边的处理
				
				//上传完成后，可以在这先对图片进行相关检查，比如是否为违禁图片
				return postHandle(request, uploadServ, module, localFilePath, partFile.getOriginalFilename());
			}
		}

        return new ResultModel(errorCode.name(), errorCode.getMsg());
	}
	
	private ResultModel postHandle(HttpServletRequest request, ProjUploadService uploadServ, String module, String localFilePath, String originName){
		try{
			//判断gif图片帧数过多的情况
		    int frames = ImageShellUtil.getFrameNumForImg(localFilePath);
		    int maxFrames = uploadServ.getMaxFrames(module);
		    if(frames > maxFrames){
		    	ThreadExecutor.addMuchFramesFlag();
		    }
		}catch(Exception e){
			LogUtils.error("get the number of frames for the image '%s' failure.", e, localFilePath);
		}
		
		RetFileInfo fileInfo = null;
		if(ThreadExecutor.isMuchFrames()){
			fileInfo = uploadServ.handleTooMuchFrameGIF(localFilePath, module, request);
		}else{
			fileInfo = uploadServ.handleReturnImg(
					localFilePath, module, request);
		}
		
		ResultModel resultModel = new ResultModel(fileInfo);
		String base64 = request.getParameter("base64");
		if("true".equals(base64)){
			String base64Code = ImageUtils.getImageStr(localFilePath);
			
			fileInfo.setBase64Code(base64Code);
		}
		
		
		/*if(!this.auditPass(fileInfo.getAccessUrl())){
			
			return new ResultModel(ImgErrorCode.PROHIBITED_IMG.name(), ImgErrorCode.PROHIBITED_IMG.getMsg());
		}*/


		String primUri = fileInfo.getPrimUri();
		String primFileLocalPath = ProjUploadService
				.getBaseStoreDir() + primUri;
		try {
			Dimension dim = ImageShellUtil.getDimension(primFileLocalPath);
			double height = dim.height;
			double width = dim.width;
			fileInfo.setWidth((int) width);
			fileInfo.setHeight((int) height);
		} catch (Exception e) {
            LogUtils.error("get dimension for image '%s'", e, primFileLocalPath);
		}

		fileInfo.getAccessUrl();  //确保accessUrl赋值成功，因为Gson只对有值的属性才生效
		fileInfo.getExtension();  //确保extension赋值成功，因为Gson只对有值的属性才生效
		fileInfo.getHumanSize();
		fileInfo.setOriginName(originName);

		if("true".equals(BaseConfig.getValue(module + ".primUri.sign.need"))){
			resultModel.addExtraInfo(ImgConstant.PRIM_URI_SIGN_KEY, PathUtils.getEncryptPrimUri(primUri));
		}

        ThreadExecutor.exec(uploadServ, localFilePath, module, request);
		
		if(ThreadExecutor.isMuchFrames()){
			ThreadExecutor.removeMuchFramesFlag(); //因为任何图片上传之后都需要经过改方法，所以，在这将gif多帧标识删除，以免内存溢出
		}
		
		String retHandler = BaseConfig.getValue(module + ".return.handler");
		try {
			RetInfoHandler retInfoHandler = BeanUtils.getBean(retHandlerMap, module, retHandler);
			if(retInfoHandler != null){
				retInfoHandler.buildRetInfo(resultModel, fileInfo);
			}
		} catch (Exception e) {
			LogUtils.error("init return hanlder for project '%s' with return handler class '%s'", e, module, retHandler);
		}
		
		return resultModel;
	}
	
	/*private boolean auditPass(String accessUrl) {
		try{
			MachineAuditInfo info = new MachineAuditInfo();
			info.setAuditContentType(AuditContentType.IMAGE);
			
			info.setImgUrl(accessUrl);
			info.setSource("upimg.mop.com/image");
			Map<String, ContentAuditResult> rs = ContentAuditClient.checkContentsByMachine(info);
			
			ContentAuditResult urlRs = rs.get("url");
			if(urlRs.getContentAuditResultType().equals(ContentAuditResultType.FORBIDDEN)){
				return false;
			}
		}catch(Exception e){
			LOG.error(e, "auditPass() error:"+e.getMessage(), "");
		}
		//如果审核接口异常，默认审核通过，不能因为审核接口，导致全站图片无法
		return true;
	}*/

	/**
	 * 
	 * @param request
	 */
	@RequestMapping(value = "/saveRuleImg")
	@ResponseBody
	public ResultModel saveRuleImg(HttpServletRequest request, HttpServletResponse response){
		String module = request.getParameter("module");
		if(StringUtils.isBlank(request.getParameter("ruleId"))){
			return new ResultModel(ImgErrorCode.ILLEGAL_RULE_ID.name(), ImgErrorCode.ILLEGAL_RULE_ID.getMsg());
		}
		
		String primUri = request.getParameter("primUri");
		/*if(!PathUtils.checkPrimUri(primUri, request.getParameter(ImgConstant.PRIM_URI_SIGN_KEY))){
			return new ResultModel(ImgErrorCode.SECURE_CHECK_NO_PASS.name(), ImgErrorCode.SECURE_CHECK_NO_PASS.getMsg());
		}*/
		
		if (isRegModule(module)) {
			String zkNewHandler = BaseConfig.getValue(module + ".handler");
			ProjUploadService uploadServ = null;
			try {
				uploadServ = BeanUtils.getBean(imgHandlerMap, module, zkNewHandler);
			} catch (Exception e) {
				LogUtils.error("init hanlder for project '%s' with handler class '%s'",
						e, module, zkNewHandler);
				
				return new ResultModel(ImgErrorCode.HANDLER_INSTANCE_ERROR.name(), ImgErrorCode.HANDLER_INSTANCE_ERROR.getMsg());
			}
				
			if(uploadServ != null){
				String localFilePath = ProjUploadService.getBaseStoreDir() + primUri;
				RetFileInfo imgInfo = uploadServ.handleReturnImg(localFilePath, module, request);
				imgInfo.getAccessUrl();  //确保accessUrl赋值成功，因为Gson只对有值的属性才生效
				imgInfo.getExtension();  //确保extension赋值成功，因为Gson只对有值的属性才生效
				imgInfo.getHumanSize();
				primUri = imgInfo.getPrimUri(); //确保primUri赋值成功，因为Gson只对有值的属性才生效
				
				ThreadExecutor.exec(uploadServ, ProjUploadService.getBaseStoreDir() + primUri, module, request);
				
				return new ResultModel(imgInfo);
			}
			
			return new ResultModel(ImgErrorCode.HANDLER_INSTANCE_ERROR.name(), ImgErrorCode.HANDLER_INSTANCE_ERROR.getMsg());
		} 
		
		return new ResultModel(ImgErrorCode.NO_SUCH_MODULE.name(), ImgErrorCode.NO_SUCH_MODULE.getMsg());
	}
	
	@RequestMapping(value = "/compositeWordToImg")
	@ResponseBody
	public ResultModel compositeWordToImg(HttpServletRequest request, HttpServletResponse response, String module){
		String regProjs = BaseConfig.getValue(REG_PROJECTS_KEY);
		if (regProjs.contains(module)) {
			int x = Integer.valueOf(request.getParameter("x"));
			int y = Integer.valueOf(request.getParameter("y"));
			int width = Integer.valueOf(request.getParameter("width"));
			int height = Integer.valueOf(request.getParameter("height"));
			String word = request.getParameter("word");
			int fontSize = Integer.valueOf(request.getParameter("fontsize"));
			int linespace = Integer.valueOf(StringUtils.isNotBlank(request.getParameter("linespace")) ? request.getParameter("linespace") : "0");
			String fontColor = StringUtils.isBlank(request.getParameter("fontcolor")) ? "black" : request.getParameter("fontcolor");
			String fontFile = BaseConfig.getValue(module + ".fontFile", "/usr/share/fonts/zh_fonts/MSYH.TTC");
			String templateFile = BaseConfig.getValue(module + ".template.dir") + "/" + request.getParameter("tplFile");
					
			if(new File(fontFile).exists()){
				String baseDir = BaseConfig.getValue("photo.local.storage.path", "/data/www/img.mop.com/img") + File.separator + module;
				
				String tmpDir = baseDir + File.separator + "tmp";
				File dir = new File(tmpDir);
				if(!dir.exists()){
					dir.mkdirs();
				}
				
				String toFileName = System.currentTimeMillis() + rand.nextInt(3) + ".png";
				String wordImgPath = tmpDir + File.separator + toFileName;
				ImageShellUtil.word2Img(word, wordImgPath, width, height, fontFile, fontSize, fontColor, linespace);
				
				File wordImg = new File(wordImgPath);
				if(wordImg.exists() && new File(templateFile).exists()){
					String replyImgDir = baseDir + File.separator + DateUtil.getCurrentDateStr();
					File replyDir = new File(replyImgDir);
					if(!replyDir.exists()){
						replyDir.mkdirs();
					}
					String replyImgPath = replyImgDir + File.separator + toFileName;
					
					ImageShellUtil.compositeImg(templateFile, wordImgPath, replyImgPath, ImgPosition.northwest, "+" + x + "+" + y);
					
					RetFileInfo imgInfo = new RetFileInfo();
					if(new File(replyImgPath).exists()){
						imgInfo.setAccessDomain(BaseConfig.getValue("photo.access.domain"));
						imgInfo.setAccessUri("/" + module + replyImgPath.replace(baseDir, ""));
						
						try {
							Dimension dim = ImageShellUtil.getDimension(replyImgPath);
							double hpx = dim.height;
							double wpx = dim.width;
							imgInfo.setWidth((int) wpx);
							imgInfo.setHeight((int) hpx);
						} catch (Exception e) {
		                    LogUtils.error("get dimension for image '%s'", e, replyImgPath);
						}
						
						imgInfo.getAccessUrl();  //确保accessUrl赋值成功，因为Gson只对有值的属性才生效
						imgInfo.getExtension();  //确保extension赋值成功，因为Gson只对有值的属性才生效
						imgInfo.getHumanSize();

						ResultModel resultModel = ResultModel.buildMapResultModel();
						resultModel.put("fileInfo", imgInfo);
						
						wordImg.delete();
						
						return resultModel;
					}
					wordImg.delete();
				}
					
				return new ResultModel(ImgErrorCode.IMG_COMPOSITE_ERROR.name(), ImgErrorCode.IMG_COMPOSITE_ERROR.getMsg());
			}
			return new ResultModel(ImgErrorCode.NO_SUCH_FONT_FILE.name(), ImgErrorCode.NO_SUCH_FONT_FILE.getMsg());
		}
		return new ResultModel(ImgErrorCode.NO_SUCH_MODULE.name(), ImgErrorCode.NO_SUCH_MODULE.getMsg());
	}
	
	public static boolean isRegModule(String module){
		String[] regProjs = BaseConfig.getValueArr(REG_PROJECTS_KEY);
		
		return regProjs != null && Arrays.asList(regProjs).contains(module);
	}

	private ImgErrorCode commonCheck(MultipartHttpServletRequest request, 
			String module) {
		
		Map<String, MultipartFile> fileMap = request.getFileMap();
		MultipartFile partFile = null;
		if (CollectionUtils.isEmpty(fileMap) || (partFile = fileMap.entrySet().iterator().next().getValue()).getSize() <= 0
				) {
			LogUtils.warn("not found any file element in the form for module %s", module);
			return ImgErrorCode.NO_FILE_ELEM;
		}

		if (!isRegModule(module)) {
			LogUtils.warn("not support module %s in white modules config %s", module, REG_PROJECTS_KEY);
			return ImgErrorCode.NO_SUCH_MODULE;
		}

		String fileName = partFile.getOriginalFilename();
		if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
			LogUtils.warn("no suffix image not supported, fileName:%s", fileName);
			return ImgErrorCode.ORIGIN_FILENAME_ILLEGAL;
		}

		String suffix = FilenameUtils.getExtension(fileName);
		if (!isImgSuffix(suffix)) {
			LogUtils.warn("image formatter not supported, fileName:%s", fileName);
			return ImgErrorCode.NOT_SUPPORT;
		}

		return ImgErrorCode.SUCCESS;
	}
	
	private boolean isImgSuffix(String suffix){
		return ("jpg".equalsIgnoreCase(suffix) || "jpeg".equalsIgnoreCase(suffix) || "png"
				.equalsIgnoreCase(suffix) || "gif".equalsIgnoreCase(suffix) || "ico".equalsIgnoreCase(suffix) || "icon".equalsIgnoreCase(suffix));
	}


	/**
	 * <Description>SaveFileFromInputStream: 将http文件流写到本地指定的目录中</Description>
	 * 
	 * @param resultObj
	 *            用户装载返回值的json对象
	 * @param stream
	 *            http文件流
	 * @param path
	 *            文件保存的目录全路径
	 * @param filename
	 *            需要被存储的文件名
	 */
	private ImgErrorCode saveFileFromInputStream(MultipartFile partFile,
			String filePath, String clientIP) {

		ImgErrorCode errorCode = ImgErrorCode.SUCCESS;
		FileOutputStream fs = null;
		InputStream stream = null;
		try {
			stream = partFile.getInputStream();
			byte[] fileBytes = IOUtils.toByteArray(stream);
			if (isIcoSuffix(filePath) || ImageShellUtil.isImageType(fileBytes)) {
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
			} else {
				errorCode = ImgErrorCode.NOT_SUPPORT;
			}
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
	
	private boolean isIcoSuffix(String fileName){
		String suffix = FilenameUtils.getExtension(fileName);
		
		return "ico".equalsIgnoreCase(suffix) || "icon".equalsIgnoreCase(suffix);
	}
}
