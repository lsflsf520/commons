package com.yisi.stiku.common.utils;

public class ScoreSagmentUtils {
	private static final int SCORE_SAGMENT_COMMON[] = { 0, 40, 80, 100, 120,150 };
	private static final int SCORE_SAGMENT_BASE[] = { 0, 40, 60, 80, 100, 120 }; // 初中
	private static final int SCORE_SAGMENT_JIANGSU_2[] = { 0, 50, 100, 130,160, 200 }; // 江苏理科
	private static final int SCORE_SAGMENT_JIANGSU_1[] = { 0, 40, 80, 100, 125,160 }; // 江苏文科
	
	public static final String[] SCORE_SAGMENT_COMMON_STR = {"（学沫）40分以下","（学弱）40~80分","（学民）80~100分","（学霸）100~120分","（学神）120分以上"}; 
	public static final String[] SCORE_SAGMENT_BASE_STR = {"（学沫）40分以下","（学弱）40~60分","（学民）60~80分","（学霸）80~100分","（学神）100分以上"}; 	
	public static final String[] SCORE_SAGMENT_JIANGSU_2_STR = {"（学沫）50分以下","（学弱）50~100分","（学民）100~130分","（学霸）130~160分","（学神）160分以上"};
	public static final String[] SCORE_SAGMENT_JIANGSU_1_STR = {"（学沫）40分以下","（学弱）40~80分","（学民）80~100分","（学霸）100~125分","（学神）125分以上"};
	
//	private static final int MAX_SCORE_200=170;
//	private static final int MAX_SCORE_160=135;
//	private static final int MAX_SCORE_150=130;
	/**
	 * 
	 * @param provinceId
	 *            省份ID
	 * @param subjectType
	 *            文理科 0文理共用 1 文科 2 理科 3 初中
	 * @return 该省份文理科所需要的分数段名称
	 */
	public static String[] getScoreSagmentArrayStr(long provinceId, int subjectType) {
		if (subjectType == 3) {
			return SCORE_SAGMENT_BASE_STR;
		} else if (provinceId == 32) {
			if (subjectType == 2) {
				return SCORE_SAGMENT_JIANGSU_2_STR;
			} else if (subjectType == 1) {
				return SCORE_SAGMENT_JIANGSU_1_STR;
			}
			return SCORE_SAGMENT_COMMON_STR;
		} else {
			return SCORE_SAGMENT_COMMON_STR;
		}
	}	
	/**
	 * 
	 * @param provinceId
	 *            省份ID
	 * @param subjectType
	 *            文理科 0文理共用 1 文科 2 理科 3 初中
	 * @return 该省份文理科所需要的分数段
	 */
	public static int[] getScoreSagmentArray(long provinceId, int subjectType) {
		if (subjectType == 3) {
			return SCORE_SAGMENT_BASE;
		} else if (provinceId == 32) {
			if (subjectType == 2) {
				return SCORE_SAGMENT_JIANGSU_2;
			} else if (subjectType == 1) {
				return SCORE_SAGMENT_JIANGSU_1;
			}
			return SCORE_SAGMENT_COMMON;
		} else {
			return SCORE_SAGMENT_COMMON;
		}
	}

	/**
	 * 
	 * @param provinceId
	 *            省份ID
	 * @param subjectType
	 *            文理科 0文理共用 1 文科 2 理科 3 初中
	 * @return 学生分数所在的分数段
	 */
	public static int getScoreSagment(long provinceId, int subjectType,
			int score) {
		int[] scoreSagment = new int[6];
		if (subjectType == 3) {
			scoreSagment = SCORE_SAGMENT_BASE;
		} else if (provinceId == 32) {
			if (subjectType == 2) {
				scoreSagment = SCORE_SAGMENT_JIANGSU_2;
			} else if (subjectType == 1) {
				scoreSagment = SCORE_SAGMENT_JIANGSU_1;
			}
		} else {
			scoreSagment = SCORE_SAGMENT_COMMON;
		}
		for (int i = 0; i < scoreSagment.length; i++) {
			if(i>=4){
				return 4;
			}
			if (score >= scoreSagment[i] && score <= scoreSagment[i + 1]) {
				return i;
			}
		}
		return 0;
	}
	/**
	 * 
	 * @param examPaperId
	 *            试卷ID
	 * @param subjectType
	 *            文理科 0文理共用 1 文科 2 理科 3 初中
	 * @return 学生分数所在的分数段
	 */
//	public static int getScoreSagmentByExam(Long examPaperId, int subjectType,
//			int score) {
//		ScoreSagmentUtils sagmentUtils=new ScoreSagmentUtils();
//		int scoreSum=sagmentUtils.getExamPaperScore(examPaperId);
//		int[] scoreSagment = new int[6];
//		if (subjectType == 3) {
//			scoreSagment = SCORE_SAGMENT_BASE;
//		} else if (scoreSum >= 200) {
//			scoreSagment = SCORE_SAGMENT_JIANGSU_2;
//		} else if (scoreSum >=160) {
//			scoreSagment = SCORE_SAGMENT_JIANGSU_1;
//		} else {
//			scoreSagment = SCORE_SAGMENT_COMMON;
//		}
//		for (int i = 0; i < scoreSagment.length; i++) {
//			if(i>=4){
//				return 4;
//			}
//			if (score >= scoreSagment[i] && score <= scoreSagment[i + 1]) {
//				return i;
//			}
//		}
//		return 0;
//	}
	/**
	 * 
	 * @param examPaperId
	 *            试卷ID
	 * @param subjectType
	 *            文理科 0文理共用 1 文科 2 理科 3 初中
	 * @return 学生分数所在的分数段
	 */
//	public static int getScoreSagmentByExamPaper(long examPaperId, int subjectType,
//			int score) {
//		int[] scoreSagment = new int[6];
//		ScoreSagmentUtils sagmentUtils=new ScoreSagmentUtils();
//		int examPaperScore=sagmentUtils.getExamPaperScore(examPaperId);
//		if (subjectType == 3) {
//			scoreSagment = SCORE_SAGMENT_BASE;
//		} else if (examPaperScore>=200) {
//				scoreSagment = SCORE_SAGMENT_JIANGSU_1;
//		} else if (subjectType>= 160) {
//				scoreSagment = SCORE_SAGMENT_JIANGSU_2;
//		} else {
//			scoreSagment = SCORE_SAGMENT_COMMON;
//		}
//		for (int i = 0; i < scoreSagment.length; i++) {
//			if(i>=4){
//				return 4;
//			}
//			if (score >= scoreSagment[i] && score <= scoreSagment[i + 1]) {
//				return i;
//			}
//		}
//		return 0;
//	}
	/**
	 * 
	 * @param examPaperId
	 *            试卷Id
	 * @param subjectType
	 *            文理科 0文理共用 1 文科 2 理科 3 初中
	 * @return 学生分数所在的分数始末分数
	 */
//	public ScoreSection getCurrentScoreSection(long examPaperId, int subjectType,
//			int score) {
//		int examPaperScore=getExamPaperScore(examPaperId);
//		if (examPaperScore >=200) {
//			return getCurrentScoreSection_jiangsu2(score);
//	   	} else if (examPaperScore >=160) {
//			return getCurrentScoreSection_jiangsu1(score);
//	   	} else {
//	   		return getCurrentScoreSection_base(score);
//	}
//	}
	/**
	 * 
	 * @param examPaperId
	 *           试卷ID
	 * @param subjectType
	 *            文理科 0文理共用 1 文科 2 理科 3 初中
	 * @return 学生分数下一分数始末分数
	 */
//	public ScoreSection getNextScoreSection(long examPaperId, int subjectType,
//			int score) {
//		int examPaperScore=getExamPaperScore(examPaperId);
//			if (examPaperScore >=200) {
//				return getNextScoreSection_jiangsu2(score);
//		   	} else if (examPaperScore>=160) {
//				return getNextScoreSection_jiangsu1(score);
//		   	} else {
//		   		return getNextScoreSection_base(score);
//		}
//	}
	
//	public int getExamPaperScore(long examPaperId){
//		int ScoreSum=SpringContextHolder.getBean(ExamPaperPromDao.class).countScore(examPaperId);
//		if(ScoreSum>=160){
//			return ScoreSum;
//		}
//		return 150;
//	}

//	private ScoreSection getCurrentScoreSection_base(Integer score) {
//		if (score < 21) {
//			return new  ScoreSection(0, 0);
//		} else if (score > 130) {
//			return new ScoreSection(120, 150);
//		} else {
//			return new ScoreSection(score - 10, score + 10);
//		}
//	}
//
//	private ScoreSection getNextScoreSection_base(Integer score) {
//		if (score < 21) {
//			return new ScoreSection(20, 40);
//		} else if (score < 41) {
//			return new ScoreSection(score + 40 - 10, score + 40 + 10);
//		} else if (score > 40 && score < 81) {
//			return new ScoreSection(score + 30 - 10, score + 30 + 10);
//		} else if (score > 80 && score < 111) {
//			return new ScoreSection(score + 20 - 10, score + 20 + 10);
//		} else if (score > 110 && score < 131) {
//			return new ScoreSection(score + 15 - 5, score + 15 + 5);
//		} else if (score > 130) {
//			return new ScoreSection(150, 150);
//		}
//		return new ScoreSection(score - 10, score + 10);
//	}
//
//	private ScoreSection getCurrentScoreSection_jiangsu2(Integer score) {
//		if (score < 21) {
//			return new  ScoreSection(0, 0);
//		} else if (score > 170) {
//			return new ScoreSection(160, 200);
//		} else {
//			return new ScoreSection(score - 10, score + 10);
//		}
//	}
//
//	private ScoreSection getNextScoreSection_jiangsu2(Integer score) {
//		if (score < 21) {
//			return new ScoreSection(20, 40);
//		} else if (score < 51) {
//			return new ScoreSection(score + 40 - 10, score + 40 + 10);
//		} else if (score > 50 && score < 101) {
//			return new ScoreSection(score + 30 - 10, score + 30 + 10);
//		} else if (score > 100 && score < 131) {
//			return new ScoreSection(score + 20 - 10, score + 20 + 10);
//		} else if (score > 130 && score < 161) {
//			return new ScoreSection(score + 15 - 5, score + 15 + 5);
//		} else if (score > 160) {
//			return new ScoreSection(200, 200);
//		}
//		return new ScoreSection(score - 10, score + 10);
//	}
//
//	private ScoreSection getCurrentScoreSection_jiangsu1(Integer score) {
//		if (score < 21) {
//			return new  ScoreSection(0, 0);
//		} else if (score > 125) {
//			return new ScoreSection(125, 160);
//		} else {
//			return new ScoreSection(score - 10, score + 10);
//		}
//	}
//
//	private ScoreSection getNextScoreSection_jiangsu1(Integer score) {
//		if (score < 21) {
//			return new ScoreSection(20, 40);
//		} else if (score < 41) {
//			return new ScoreSection(score + 40 - 10, score + 40 + 10);
//		} else if (score > 40 && score < 81) {
//			return new ScoreSection(score + 30 - 10, score + 30 + 10);
//		} else if (score > 80 && score < 111) {
//			return new ScoreSection(score + 20 - 10, score + 20 + 10);
//		} else if (score > 110 && score < 131) {
//			return new ScoreSection(score + 15 - 5, score + 15 + 5);
//		} else if (score > 125) {
//			return new ScoreSection(160, 160);
//		}
//		return new ScoreSection(score - 10, score + 10);
//	}
	
	/**
	 * 
	 * @param examPaperId
	 *           试卷ID
	 * @param subjectType
	 *            文理科 0文理共用 1 文科 2 理科 3 初中
	 * @return 该试卷学生最高有效分数
	 */
//	public  int getMaxScoreSection(long examPaperId, int subjectType) {
//		int examPaperScore=getExamPaperScore(examPaperId);
//			if (examPaperScore >=200) {
//				return MAX_SCORE_200;
//		   	} else if (examPaperScore>=160) {
//				return MAX_SCORE_160;
//		   	} else {
//		   		return MAX_SCORE_150;
//		}
//	}

	/*public class ScoreSection {
		private Integer beginScore;
		private Integer endScore;

		public ScoreSection(int beginScore, int endScore) {
			this.beginScore = beginScore;
			this.endScore = endScore;
		}

		public Integer getEndScore() {
			return endScore;
		}

		public void setEndScore(Integer endScore) {
			this.endScore = endScore;
		}

		public Integer getBeginScore() {
			return beginScore;
		}

		public void setBeginScore(Integer beginScore) {
			this.beginScore = beginScore;
		}
	}*/
}
