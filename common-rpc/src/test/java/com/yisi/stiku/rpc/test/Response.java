package com.yisi.stiku.rpc.test;


/**
 * @author shangfeng
 *
 */
public class Response {

	// private SolrDocumentList _results = null;

	private SDL sdl = null;

	/*
	 * public SolrDocumentList get_results() {
	 * 
	 * return _results; }
	 * 
	 * public void set_results(SolrDocumentList _results) {
	 * 
	 * this._results = _results; }
	 */

	public SDL getSdl() {

		return sdl;
	}

	public void setSdl(SDL sdl) {

		this.sdl = sdl;
	}

}
