package com.ott.canal.test;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import javax.jws.WebService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Created by dongcunxiao on 2017/9/6 11:21
 */
public class TestServlet extends HttpServlet {
    public TestServlet() {

        System.out.println("++++++++++++++++++构造方法+++++++++++++++++++++++");
    }

    @Override
    public void init() throws ServletException {

        System.out.println("+++++++++++++++++++++测试serle  初始化=++++++++++++++++++");
        super.init();
    }
}
