package com.ujigu.secure.upfile.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.Gson;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.BeanUtils;
import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.secure.common.utils.ImageUtils;
import com.ujigu.secure.upfile.bean.ImgConstant;
import com.ujigu.secure.upfile.bean.ImgErrorCode;
import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.upfile.service.ProjUploadService;
import com.ujigu.secure.upfile.service.RetInfoHandler;
import com.ujigu.secure.upfile.util.ImagePathUtils;
import com.ujigu.secure.upfile.util.ImageShellUtil;
import com.ujigu.secure.upfile.util.MD5Util;
import com.ujigu.secure.upfile.util.PathUtils;
import com.ujigu.secure.upfile.util.ThreadExecutor;
import com.ujigu.secure.web.util.WebUtils;

@Controller
@RequestMapping("/file")
public class UpImgController {
//	@Resource 
//	private CommonsMultipartResolver multipartResolver;
	
	private final static Logger LOG = LoggerFactory
			.getLogger(UpImgController.class);
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
					LOG.error("init hanlder for project '" + project
							+ "' with handler class '" + handlerClz + "'",
							IPUtil.getLocalIp(), e);
				}
			}
		}
	}

	@RequestMapping(value = "/getDelImgFile")
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
	}
	@RequestMapping(value = "/getImgInfo")
	@ResponseBody
	public ResultModel getImgInfo(String imgUri, 
			HttpServletResponse response) throws IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");
        
		String imgPath = this.getImgPath(imgUri);
		
		if(StringUtils.isBlank(imgPath)){
			//路径不存在
			return new ResultModel("NOT_EXIST", "文件 " + imgUri + " 不存在");
		}
		
		File f = new File(imgPath);
		if (!f.isFile() || !f.exists()) {
			imgPath = backupBasePath + imgPath;
			f = new File(imgPath);
			if (!f.isFile() || !f.exists()) {
				//路径不存在
				return new ResultModel("NOT_EXIST", "文件 " + imgUri + " 不存在");
			}
		}
		
		String md5 = MD5Util.md5sum(imgPath);
		String base64Code = ImageUtils.getImageStr(imgPath);
		ResultModel resultModel = ResultModel.buildMapResultModel();
		resultModel.put("md5", md5);
		resultModel.put("base64Code", base64Code);
		return resultModel;
	}
	
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
			return new ResultModel("NOT_EXIST", "文件 " + imgUrl + " 不存在");
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
				LOG.error("recover photo error errMsg="+e.getMessage(), e);
				return new ResultModel("RECOVER_ERR", "文件 " + imgUrl + " 恢复失败！");
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
			return new ResultModel("NOT_EXIST", "文件 " + imgUrl + " 不存在");
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
				LOG.error("删除文件异常，url="+imgUrl, e);
				//异常
				return new ResultModel("NOT_EXIST", "文件 " + imgUrl + " 删除失败");
			}
		}
		return new ResultModel(true);
	}

	private String getImgPath(String imgUri) throws MalformedURLException {
		String baseDir = BaseConfig.getValue("photo.local.storage.path");
		return baseDir+imgUri;
	}
	
	private ResultModel uploadFile(MultipartHttpServletRequest request, String prefix, String module, boolean needImgCheck){
		ResultModel resultModel = ResultModel.buildMapResultModel();
		
		String clientIP = WebUtils.getIpAddr(request);
		Map<String, MultipartFile> fileMap = request.getFileMap();
		String upfileName = request.getParameter(ImgConstant.UP_FILE_ELEM_NAME_KEY);
		MultipartFile partFile = fileMap.get(upfileName);

		String zkNewHandler = BaseConfig.getValue(module + ".handler");
		ProjUploadService uploadServ = null;
		try {
		    uploadServ = BeanUtils.getBean(imgHandlerMap, module, zkNewHandler);
		} catch (Exception e) {
			LOG.error("init hanlder for project '" + module
					+ "' with handler class '" + zkNewHandler + "'"+
					IPUtil.getLocalIp(), e);
		}

		if(uploadServ == null){
			return new ResultModel(ImgErrorCode.HANDLER_INSTANCE_ERROR.name(), "没有找到合适的处理器");
		}

		String localFilePath = uploadServ.getLocalFilePath(partFile, prefix,
				module);

		PathUtils.checkDirs(FilenameUtils.getFullPathNoEndSeparator(localFilePath)); // 检查目录是否存在,若不存在，则创建之

		ImgErrorCode errorCode = saveFileFromInputStream(partFile, localFilePath,
				clientIP, needImgCheck);

		if (ImgErrorCode.SUCCESS.equals(errorCode)) {
			RetFileInfo imgInfo = uploadServ.handleReturnImg(
						localFilePath, module, request);
			imgInfo.getAccessUrl();  //确保accessUrl赋值成功，因为Gson只对有值的属性才生效
			imgInfo.getExtension();  //确保extension赋值成功，因为Gson只对有值的属性才生效
			imgInfo.getHumanSize();
			imgInfo.setOriginName(partFile.getOriginalFilename());
			String base64 = request.getParameter("base64");
			if("true".equals(base64)){
				String base64Code = ImageUtils.getImageStr(localFilePath);
				imgInfo.setBase64Code(base64Code);
			}
			resultModel.put("fileInfo", imgInfo);
			return resultModel;
		}
		
		return new ResultModel(errorCode.name(), errorCode.getMsg());
	}
	
	@RequestMapping(value = "/uploadfile", method = RequestMethod.POST)
	@ResponseBody
	public ResultModel uploadfile(MultipartHttpServletRequest request,
			HttpServletResponse response, String prefix, String module) throws IOException {
		/*response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");

		String module = request.getParameter("module");
		
		return uploadFile(request, module, false);*/
		
		ResultModel resultModel = new FileController().uploadfile(request, response, prefix, module);
		ResultModel rm = ResultModel.buildMapResultModel();
		rm.put("fileInfo", resultModel.getModel());
		
		return rm;
	}

	@RequestMapping(value = "/uploadImg", method = RequestMethod.POST)
	@ResponseBody
	public ResultModel uploadImg(MultipartHttpServletRequest request,
			HttpServletResponse response, String prefix) throws IOException {

		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");

		String module = request.getParameter("module");
		// String domain = request.getServerName(); // 获取当前的域名
		ImgErrorCode errorCode = commonCheck(request, module);
		if (!ImgErrorCode.SUCCESS.equals(errorCode)) {
			return new ResultModel(errorCode.name(), errorCode.getMsg());
		}

		ResultModel resultModel = uploadFile(request, prefix, module, true);
		resultModel.put(ImgConstant.ERROR_CODE_KEY, errorCode);
		String retHandler = BaseConfig.getValue(module + ".return.handler");
		try {
			RetInfoHandler retInfoHandler = BeanUtils.getBean(retHandlerMap, module, retHandler);
			if(retInfoHandler != null){
				retInfoHandler.buildRetInfo(resultModel, (RetFileInfo)resultModel.getValue("fileInfo"));
			}
		} catch (Exception e) {
			LOG.error("init return hanlder for project '" + module
					+ "' with return handler class '" + retHandler + "'" +
					IPUtil.getLocalIp(), e);
		}
		return resultModel;
	}
	
	@RequestMapping(value = "/uploadTest", method = RequestMethod.POST)
	public void uploadTest(MultipartHttpServletRequest request,
			HttpServletResponse response, String prefix) throws IOException{
		String module = request.getParameter("module");
		// String domain = request.getServerName(); // 获取当前的域名
		ImgErrorCode errorCode = commonCheck(request, module);
		String msgInfo = "出现错误";
		if (!ImgErrorCode.SUCCESS.equals(errorCode)) {
			msgInfo = errorCode.getMsg();
		}

		ResultModel resultModel = uploadFile(request, prefix, module, true);
		msgInfo = new Gson().toJson(resultModel);
		String retInfo = "<html><head>" +
		               "<script src='"+request.getParameter("script")+"' type='text/javascript'></script>" +
				       "</head><body>" +
				       msgInfo + 
		               "</body></html>";
		WebUtils.writeJson(retInfo, request, response);
	}
	
	@RequestMapping(value = "/uploadImg4KE", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> uploadImg4KE(MultipartHttpServletRequest request,
			HttpServletResponse response, String prefix) throws IOException{
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with");

        Map<String, Object> retJson = new HashMap<>();
        retJson.put("error", 1);
        
		String module = request.getParameter("module");
		// String domain = request.getServerName(); // 获取当前的域名
		ImgErrorCode errorCode = commonCheck(request, module);
		if (!ImgErrorCode.SUCCESS.equals(errorCode)) {
			retJson.put("message", errorCode.getMsg());
			return retJson;
		}

		ResultModel resultModel = uploadFile(request, prefix, module, true);
//		resultModel.put(ImgConstant.ERROR_CODE_KEY, errorCode);
		RetFileInfo fileInfo = (RetFileInfo)resultModel.getValue("fileInfo");
		if(fileInfo != null){
			retJson.put("error", 0);
			retJson.put("url", fileInfo.getAccessUrl());
		}else{
			retJson.put("message", "上传失败");
		}
		return retJson;
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
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(ImgConstant.ERROR_CODE_KEY, ImgErrorCode.SUCCESS);
		
		String module = request.getParameter("module");
		String regProjs = BaseConfig.getValue(REG_PROJECTS_KEY);
		if(StringUtils.isBlank(request.getParameter("ruleId"))){
			resultMap.put(ImgConstant.ERROR_CODE_KEY, ImgErrorCode.ILLEGAL_RULE_ID);
			return new ResultModel(ImgErrorCode.ILLEGAL_RULE_ID.name(), "参数错误！");
		}
		
		String primUri = request.getParameter("primUri");
		if(!PathUtils.checkPrimUri(primUri, request.getParameter(ImgConstant.PRIM_URI_SIGN_KEY))){
			return new ResultModel(ImgErrorCode.SECURE_CHECK_NO_PASS.name(), "安全校验失败！");
		}
		
		if (regProjs.contains(module)) {
			String zkNewHandler = BaseConfig.getValue(module + ".handler");
			ProjUploadService uploadServ = null;
			try {
				uploadServ = BeanUtils.getBean(imgHandlerMap, module, zkNewHandler);
			} catch (Exception e) {
				LOG.error("init hanlder for project '" + module
						+ "' with handler class '" + zkNewHandler + "'"+
						IPUtil.getLocalIp(), e);
				
				resultMap.put(ImgConstant.ERROR_CODE_KEY, ImgErrorCode.HANDLER_INSTANCE_ERROR);
			}
				
			RetFileInfo imgInfo = null;
			if(uploadServ != null){
				String localFilePath = uploadServ.getBaseStoreDir() + primUri;
				imgInfo = uploadServ.handleReturnImg(localFilePath, module, request);
				imgInfo.getAccessUrl();  //确保accessUrl赋值成功，因为Gson只对有值的属性才生效
				imgInfo.getExtension();  //确保extension赋值成功，因为Gson只对有值的属性才生效
				imgInfo.getHumanSize();
				primUri = imgInfo.getPrimUri(); //确保primUri赋值成功，因为Gson只对有值的属性才生效
				resultMap.put("imgInfo", imgInfo);
				
				ThreadExecutor.exec(uploadServ, uploadServ.getBaseStoreDir() + primUri, module, request);
			}
			
			return new ResultModel(imgInfo);
		}
		
		return new ResultModel(ImgErrorCode.NO_SUCH_MODULE.name(), "不支持的参数！");
	}
	
	/*@RequestMapping(value = "/compositeWordToImg")
	public void compositeWordToImg(HttpServletRequest request, HttpServletResponse response, String module){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String regProjs = ConfigOnZk.getInstance().getValue(ImgConstant.ZK_SYSTEM_PATH,
				REG_PROJECTS_KEY);
		if (regProjs.contains(module)) {
			int x = Integer.valueOf(request.getParameter("x"));
			int y = Integer.valueOf(request.getParameter("y"));
			int width = Integer.valueOf(request.getParameter("width"));
			int height = Integer.valueOf(request.getParameter("height"));
			String word = request.getParameter("word");
			int fontSize = Integer.valueOf(request.getParameter("fontsize"));
			int linespace = Integer.valueOf(StringUtils.isNotBlank(request.getParameter("linespace")) ? request.getParameter("linespace") : "0");
			String fontColor = StringUtils.isBlank(request.getParameter("fontcolor")) ? "black" : request.getParameter("fontcolor");
			String fontFile = ConfigOnZk.getInstance().getValue(ImgConstant.ZK_SYSTEM_PATH, module + ".fontFile", "/usr/share/fonts/zh_fonts/MSYH.TTC");
			String templateFile = ConfigOnZk.getInstance().getValue(ImgConstant.ZK_SYSTEM_PATH, module + ".template.dir") + "/" + request.getParameter("tplFile");
					
			if(new File(fontFile).exists()){
				String baseDir = ConfigOnZk.getInstance().getValue(ImgConstant.ZK_SYSTEM_PATH,
						"photo.local.storage.path", "/data/www/img.mop.com/img") + File.separator + module;
				
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
					
					ReturnImgInfo imgInfo = new ReturnImgInfo();
					if(new File(replyImgPath).exists()){
						imgInfo.setAccessDomain(ConfigOnZk.getInstance().getValue(ImgConstant.ZK_SYSTEM_PATH, "photo.access.domain"));
						imgInfo.setAccessUri("/" + module + replyImgPath.replace(baseDir, ""));
						
						try {
							Dimension dim = ImageShellUtil.getDimension(replyImgPath);
							double hpx = dim.height;
							double wpx = dim.width;
							imgInfo.setWidth((int) wpx);
							imgInfo.setHeight((int) hpx);
						} catch (Exception e) {
		                    LOG.error(e, "get dimension for image '" + replyImgPath, WebUtils.getIpAddr(request));
						}
						
						imgInfo.getAccessUrl();  //确保accessUrl赋值成功，因为Gson只对有值的属性才生效
						imgInfo.getExtension();  //确保extension赋值成功，因为Gson只对有值的属性才生效
						imgInfo.getHumanSize();

						resultMap.put("imgInfo", imgInfo);
					}else{
						resultMap.put(ImgConstant.ERROR_CODE_KEY, ImgErrorCode.IMG_COMPOSITE_ERROR);
					}
				}
				
				wordImg.delete();
			}else{
				resultMap.put(ImgConstant.ERROR_CODE_KEY, ImgErrorCode.NO_SUCH_FONT_FILE);
			}
		}else{
			resultMap.put(ImgConstant.ERROR_CODE_KEY, ImgErrorCode.NO_SUCH_PROJECT);
		}
		
		WebUtils.writeJsonByObj(resultMap, response, request);
	}*/

	private ImgErrorCode commonCheck(MultipartHttpServletRequest request,
			String module) {
		Map<String, MultipartFile> fileMap = request.getFileMap();
		String upfileName = request.getParameter(ImgConstant.UP_FILE_ELEM_NAME_KEY);
		if (fileMap == null || fileMap.size() <= 0
				|| StringUtils.isBlank(upfileName)
				|| fileMap.get(upfileName) == null) {
			return ImgErrorCode.NO_FILE_ELEM;
		}

		String regProjs = BaseConfig.getValue(
				REG_PROJECTS_KEY);
		if (!regProjs.contains(module)) {
			return ImgErrorCode.NO_SUCH_MODULE;
		}

		String fileName = fileMap.get(upfileName).getOriginalFilename();
		if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
			return ImgErrorCode.ORIGIN_FILENAME_ILLEGAL;
		}

		if (!hasImgSuffix(fileName)) {
			LOG.warn("image formatter not supported", "fileName:" + fileName,
					WebUtils.getIpAddr(request));
			return ImgErrorCode.NOT_SUPPORT;
		}

		return ImgErrorCode.SUCCESS;
	}
	
	private boolean hasImgSuffix(String fileName){
		String suffix = FilenameUtils.getExtension(fileName);
		
		return "jpg".equalsIgnoreCase(suffix) || "jpeg".equalsIgnoreCase(suffix) || "png"
				.equalsIgnoreCase(suffix) || "gif".equalsIgnoreCase(suffix) || isIcoSuffix(fileName);
	}
	
	private boolean isIcoSuffix(String fileName){
		String suffix = FilenameUtils.getExtension(fileName);
		
		return "ico".equalsIgnoreCase(suffix) || "icon".equalsIgnoreCase(suffix);
	}
	
	/*public static void main(String[] args) throws IOException {
		FileInputStream fs = new FileInputStream(new File("D:\\Downloads\\36258373_1414030883_uglM.jpg"));
		byte[] fileBytes = IOUtils.toByteArray(fs);
		
		boolean isImage = ImageShellUtil.isImageType(fileBytes);
		int count = 0;
		for(byte b : fileBytes){
			System.out.print(b+ " ");
			if(++count % 8 == 0){
				System.out.println();
			}
			if(count > 20){
				break;
			}
		}
		
	}*/


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
			String filePath, String clientIP, boolean needImgCheck) {

		ImgErrorCode errorCode = ImgErrorCode.SUCCESS;
		FileOutputStream fs = null;
		InputStream stream = null;
		try {
			stream = partFile.getInputStream();
			byte[] fileBytes = IOUtils.toByteArray(stream);
//			boolean isImage = ;
			if (!needImgCheck || isIcoSuffix(filePath) || ImageShellUtil.isImageType(fileBytes)) {
				fs = new FileOutputStream(filePath);
				fs.write(fileBytes);

				LOG.debug("file '" + filePath
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
			LOG.error("path:" + filePath + " " + clientIP, e);
			errorCode = ImgErrorCode.SAVE_IO_EXCEPTION;
		} finally {
			if (fs != null) {
				try {
					fs.flush();
				} catch (IOException e) {
					LOG.error("path:" + filePath+" "+clientIP, e);
				}
			}
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					LOG.error("path:" + filePath+" "+clientIP, e);
				}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					LOG.error("path:" + filePath+" "+clientIP, e);
				}
			}

		}

		return errorCode;
	}

}
