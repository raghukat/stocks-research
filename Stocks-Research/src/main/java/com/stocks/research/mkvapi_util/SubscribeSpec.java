//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SubscribeSpec {
    String chain1;
    String chain2;
    String reportName;
    List<String> fields = new ArrayList();

    public SubscribeSpec(String file) throws Exception {
        this.loadSubscribeSpec(file);
    }

    private void loadSubscribeSpec(String file) throws Exception {
        BufferedReader rd = new BufferedReader(new FileReader(file));

        String s;
        while((s = rd.readLine()) != null) {
            s = s.trim();
            if (s.length() != 0 && !s.startsWith("#")) {
                if (s.startsWith(">")) {
                    this.reportName = this.substitute(s.substring(1));
                } else if (s.startsWith("@")) {
                    if (this.chain1 == null) {
                        this.chain1 = this.substitute(s.substring(1));
                    } else {
                        this.chain2 = this.substitute(s.substring(1));
                    }
                } else {
                    this.fields.add(s);
                }
            }
        }

    }

    String substitute(String s) {
        String ccy = System.getProperty("CCY");
        return s.contains("%CCY%") ? s.replaceAll("%CCY%", ccy) : s;
    }
}
