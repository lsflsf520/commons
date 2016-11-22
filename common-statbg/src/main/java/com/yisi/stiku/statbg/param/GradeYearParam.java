package com.yisi.stiku.statbg.param;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.StudentUtils;
import com.yisi.stiku.statbg.GlobalParam;

/**
 * @author shangfeng
 *
 */
public class GradeYearParam implements GlobalParam<Integer> {

	private int grade; // 1(高一)、2(高二)、3(高三)

	@Override
	public Integer generateParam() {

		if (grade < 1 || grade > 3) {
			throw new BaseRuntimeException("NOT_SUPPORT", "年级只支持 1(高一)、2(高二)、3(高三)");
		}
		return StudentUtils.getGrade(grade);
	}

	public int getGrade() {

		return grade;
	}

	public void setGrade(int grade) {

		this.grade = grade;
	}

}
