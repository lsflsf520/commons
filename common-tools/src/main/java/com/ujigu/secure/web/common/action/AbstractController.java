package com.ujigu.secure.web.common.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ujigu.secure.common.bean.BaseEntity;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.db.bean.PageData;
import com.ujigu.secure.db.service.AbstractBaseService;
import com.ujigu.secure.db.service.IExtraBaseService;

/**
 * 
 * @author lsf
 *
 */

abstract public class AbstractController<PK extends Serializable, T extends BaseEntity<PK>> {

	private String module;

	public AbstractController() {
		RequestMapping rm = this.getClass().getAnnotation(RequestMapping.class);
		if (rm == null) {
			throw new BaseRuntimeException("ILLEGAL_CONFIG",
					"Not Found RequestMapping annotation in class " + this.getClass().getName());
		}
		String[] modules = rm.value();
		if (modules == null || modules.length <= 0 || StringUtils.isBlank(modules[0])) {
			throw new BaseRuntimeException("ILLEGAL_CONFIG",
					"module path not defined for RequestMapping annotation in class " + this.getClass().getName());
		}

		this.module = modules[0];
		if(this.module.startsWith("mgr/")){
			this.module = this.module.substring("mgr/".length());
		}else if(this.module.startsWith("/mgr/")){
			this.module = this.module.substring("/mgr/".length());
		}
	}

	/**
	 * 本方法默认只返回 dataPage 和 queryData 这两个对象，如果还有其它额外的参数需要返回，请重写 @see loadListOtherData 方法
	 * @param request
	 * @param response
	 * @param queryData
	 * @return
	 */
	@RequestMapping("list")
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response, T queryData) {
		PageData<T> dataPage = loadListData(request, response, queryData);

		ModelAndView mav = new ModelAndView(this.module + "_list");
		mav.addObject("dataPage", dataPage);
		mav.addObject("queryData", queryData);

		Map<String, ?> otherData = afterList(request, response, dataPage);
		if (!CollectionUtils.isEmpty(otherData)) {
			mav.addAllObjects(otherData);
		}

		return mav;
	}

	@RequestMapping("toedit")
	public ModelAndView toedit(HttpServletRequest request, HttpServletResponse response, String srcTabId, PK pk) {
		T dbData = null;
		if (pk != null) {
			dbData = loadData(request, response, pk);
		}

		ModelAndView mav = new ModelAndView(this.module + "_edit");
		mav.addObject("dbData", dbData);
		mav.addObject("srcTabId", srcTabId);

		Map<String, ?> otherData = afterToEdit(request, response, dbData);
		if (!CollectionUtils.isEmpty(otherData)) {
			mav.addAllObjects(otherData);
		}

		return mav;
	}

	@RequestMapping("dosave")
	@ResponseBody
	public ResultModel save(HttpServletRequest request, HttpServletResponse response, @Valid T formData) {
		PK pk = doSave(request, response, formData);

		if (pk == null) {
			return new ResultModel(false);
		}
		return new ResultModel(pk);
	}
	
	@RequestMapping("remove")
	@ResponseBody
	@SuppressWarnings("all")
	public ResultModel remove(HttpServletRequest request, HttpServletResponse response, PK... pks) {
		if (pks == null || pks.length <= 0) {
			return new ResultModel("ILLEGAL_PARAM", "参数错误");
		}

		AbstractBaseService<PK, T> busiService = getBusiService();

		busiService.batchDel(pks);

		return new ResultModel(true);
	}

	@RequestMapping("del")
	@ResponseBody
	@SuppressWarnings("all")
	public ResultModel del(HttpServletRequest request, HttpServletResponse response, PK... pks) {
		if (pks == null || pks.length <= 0) {
			return new ResultModel("ILLEGAL_PARAM", "参数错误");
		}

		AbstractBaseService<PK, T> busiService = getBusiService();

		busiService.softDel(pks);

		return new ResultModel(true);
	}
	
	@RequestMapping("invalid")
	@ResponseBody
	@SuppressWarnings("all")
	public ResultModel invalid(HttpServletRequest request, HttpServletResponse response, PK... pks) {
		if (pks == null || pks.length <= 0) {
			return new ResultModel("ILLEGAL_PARAM", "参数错误");
		}

		AbstractBaseService<PK, T> busiService = getBusiService();

		busiService.invalid(pks);

		return new ResultModel(true);
	}
	
	@RequestMapping("close")
	@ResponseBody
	@SuppressWarnings("all")
	public ResultModel close(HttpServletRequest request, HttpServletResponse response, PK... pks) {
		if (pks == null || pks.length <= 0) {
			return new ResultModel("ILLEGAL_PARAM", "参数错误");
		}

		AbstractBaseService<PK, T> busiService = getBusiService();

		busiService.close(pks);

		return new ResultModel(true);
	}
	
	@RequestMapping("freeze")
	@ResponseBody
	@SuppressWarnings("all")
	public ResultModel freeze(HttpServletRequest request, HttpServletResponse response, PK... pks) {
		if (pks == null || pks.length <= 0) {
			return new ResultModel("ILLEGAL_PARAM", "参数错误");
		}

		AbstractBaseService<PK, T> busiService = getBusiService();

		busiService.freeze(pks);

		return new ResultModel(true);
	}
	
	@RequestMapping("recover")
	@ResponseBody
	@SuppressWarnings("all")
	public ResultModel recover(HttpServletRequest request, HttpServletResponse response, PK... pks) {
		if (pks == null || pks.length <= 0) {
			return new ResultModel("ILLEGAL_PARAM", "参数错误");
		}

		AbstractBaseService<PK, T> busiService = getBusiService();

		busiService.softRecover(pks);

		return new ResultModel(true);
	}

	/**
	 * 根据queryData返回符合要求的分页对象
	 * @param request
	 * @param response
	 * @param queryData
	 * @return 
	 */
	protected PageData<T> loadListData(HttpServletRequest request, HttpServletResponse response, T queryData) {
		AbstractBaseService<PK, T> busiService = getBusiService();

		return busiService.findByPage(queryData);
	}

	/**
	 * 执行list方法时，如果还有额外的其它参数需要返回或者其它逻辑需要处理，子类可以重写该方法
	 * @param request
	 * @param response
	 * @param queryData
	 * @return 
	 */
	protected Map<String, ?> afterList(HttpServletRequest request, HttpServletResponse response,
			PageData<T> dataPage) {
		return new HashMap<>();
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param pk
	 * @return
	 */
	protected T loadData(HttpServletRequest request, HttpServletResponse response, PK pk) {
		AbstractBaseService<PK, T> busiService = getBusiService();

		return (T) busiService.findById(pk);
	}

	/**
	 * 注意，此处的参数t可能会为空
	 * 在进入编辑页面时，默认只返回 dbData参数，如果还有额外的其它数据需要返回，请重写此方法
	 * @param t
	 * @return
	 */
	protected Map<String, ?> afterToEdit(HttpServletRequest request, HttpServletResponse response, T t) {
		return new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	protected PK doSave(HttpServletRequest request, HttpServletResponse response, T formData) {
		AbstractBaseService<PK, T> busiService = getBusiService();

		return ((IExtraBaseService<PK, T>) busiService).doSave(formData);
	}
	
	abstract protected AbstractBaseService<PK, T> getBusiService();
}
