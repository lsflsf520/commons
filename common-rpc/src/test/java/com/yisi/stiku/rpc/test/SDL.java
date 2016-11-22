package com.yisi.stiku.rpc.test;

/**
 * @author shangfeng
 *
 */
public class SDL
{

	private long numFound = 0;
	private long start = 0;
	private Float maxScore = null;

	public Float getMaxScore() {

		return maxScore;
	}

	public void setMaxScore(Float maxScore) {

		this.maxScore = maxScore;
	}

	public long getNumFound() {

		return numFound;
	}

	public void setNumFound(long numFound) {

		this.numFound = numFound;
	}

	public long getStart() {

		return start;
	}

	public void setStart(long start) {

		this.start = start;
	}

	@Override
	public String toString() {

		return "{numFound=" + numFound
				+ ",start=" + start
				+ (maxScore != null ? ",maxScore=" + maxScore : "")
				+ ",docs=" + super.toString()
				+ "}";
	}
}
