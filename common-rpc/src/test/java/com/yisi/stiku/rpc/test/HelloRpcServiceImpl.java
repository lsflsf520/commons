package com.yisi.stiku.rpc.test;

import org.springframework.stereotype.Component;

import com.yisi.stiku.rpc.annotation.RpcService;

@Component
@RpcService(HelloRpcService.class)
public class HelloRpcServiceImpl implements HelloRpcService {

	@Override
	public String sayHello(String name, int count) {

		// System.out.println("received name '" + name + "'");

		// String prefix = "hello " + name;
		// StringBuilder builder = new StringBuilder(prefix);
		// builder.append(RandomUtil.randomNumCode(count - prefix.length()));
		// return builder.toString();

		return "5295292051864423894919848455955293630483633947786677991784621322227776063486962147191300619418197223534298632423517885800672217099390313243553297083018846640230747302609303433738238122337456343747650430895129421304914947096421272561847973442543260323523634513162147297924984691245879983122270973171768148994956240322675921641350887408737951235818139310052340057815382936910791249731861946335012157803809967721087193952856079919438432056082281233331232491855434024806584631006515519479563128845019966758727065150634008135954672644169776852244111795166770252570736952591614167626099552983374049263452256866738101629423236113057482265796800425092279618872719178483780761198033943842091668582835814580111767136448102617145494740623887418691762216605121882061310520730785424505598394322643302456995558395968282097572732158175281808685411824310375361727346089721147993867740711883392145366266991680701833508820107417229013013620524472131366258581161811177274920363750020992649867861194535204799390561845816422027377602593183996610";
	}

	@Override
	public Person getPerson() {

		Person p = new Person();
		p.setHeight(23);
		p.setName("lsf");
		p.setWeight(65);
		return p;
	}

	@Override
	public Car showCar() {

		Car car = new Car();
		car.setHeight(50);
		car.setLength(100);
		car.setWidth(35);
		car.setPinpai("dazhong");

		return car;
	}

	@Override
	public Response query(int start) {

		Response res = null;

		/*
		 * SolrDocumentList docList = new SolrDocumentList(); SolrDocument doc =
		 * new SolrDocument(); doc.setField("name", "鹤顶红"); doc.setField("type",
		 * "毒药"); docList.add(doc);
		 */

		SDL sdl = new SDL();
		sdl.setMaxScore(1.0f);
		sdl.setNumFound(1124);
		sdl.setStart(start);

		/*
		 * docList.setMaxScore(1.0f); docList.setNumFound(1124);
		 * docList.setStart(start);
		 * 
		 * res.set_results(docList);
		 */
		res.setSdl(sdl);

		return res;
	}

	// @Override
	// public boolean save(TblSysMenu sysmenu) {
	// // TODO Auto-generated method stub
	// return true;
	// }

}
