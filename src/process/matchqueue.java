package process;

import dataprocess.dom4j;
import dataprocess.json;
import network.network;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class matchqueue {
    private user u;
    private boolean isqueuing = false;
    private String queuename;
    private int queueineed;
    private int queueid;

    public matchqueue(user u, String queuename, int queueinneed) {
        this.u = u;
        this.queuename = queuename;
        this.queueineed = queueinneed;
    }

    public void process(JSONObject doc) {
        if (doc.getString("func").equals("join")) {
            this.join();
            while (isqueuing) {
                if (u.socket.isConnected()) {
                    try {
                        if (isfull()) break;
                        new network(u.socket).send(json.jsonaesencrypet(json.jsonaddjson(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"102", "Waiting", "matchqueue", "wait"}), "parameter", XML.toJSONObject(dom4j.xmltostring(this.getQueueInfo(), dom4j.makeOF()))), u.getToken()).toString());
                        Thread.sleep(500);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.quit_queue();
                    break;
                }
            }
            if (u.socket.isConnected()) {
                try {
                    new network(u.socket).send(json.jsonaesencrypet(json.jsonaddjson(json.makejson(new String[]{"status", "message", "class", "func"}, new String[]{"200", "Matched", "matchqueue", "matched"}), "parameter", XML.toJSONObject(dom4j.xmltostring(this.getQueueInfo(), dom4j.makeOF()))), u.getToken()).toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private String getCONFIGfile() {
        File md = new File("data/queues/" + queuename + "/" + queueineed);
        md.mkdirs();
        return "data/queues/" + queuename + "/" + queueineed + "/config.xml";
    }
    private String getQUEUEfile(int queueid) {
        File md = new File("data/queues/" + queuename + "/" + queueineed);
        md.mkdirs();
        return "data/queues/" + queuename + "/" + queueineed + "/" + queueid + ".queue.xml";
    }
    private void createqueue() {
        Document config = dom4j.lock(getCONFIGfile());
        AtomicInteger nowqueuecreated = new AtomicInteger();
        if (config != null) {
            config.getRootElement().element("itor").setText((Integer.parseInt(config.getRootElement().element("itor").getText()) + 1) + "");
            nowqueuecreated.set(Integer.parseInt(config.getRootElement().element("itor").getText()));
        } else {
            config = DocumentHelper.createDocument();
            Element root = config.addElement("queuecfg");
            root.addElement("itor").setText("1");
            root.addElement("lock").setText("unlock");
            nowqueuecreated.set(1);
        }
        dataprocess.dom4j.unlock(config, getCONFIGfile());
        int nowqueueid = nowqueuecreated.get();
        Document newqueue = DocumentHelper.createDocument();
        Element rootofqueue = newqueue.addElement("queue");
        for (AtomicInteger x = new AtomicInteger(1); x.get() <= this.queueineed; x.getAndIncrement())
            rootofqueue.addElement("number-" + x.get()).setText("empty");
        rootofqueue.addElement("isfull").setText("no");
        dataprocess.dom4j.unlock(newqueue, getQUEUEfile(nowqueueid));
        log.printf("Created Queue ID:" + nowqueueid + " Name:" + queuename + " MemberNumber:" + queueineed, Thread.currentThread().getStackTrace()[1].getFileName(), Thread.currentThread().getStackTrace()[1].getLineNumber());
    }

    private void join() {
        while (!isqueuing) {
            Document doc = dom4j.lock(getCONFIGfile());
            if (doc != null) {
                int now = Integer.parseInt(doc.getRootElement().element("itor").getText());
                Document queue = dom4j.lock(getQUEUEfile(now));
                if (queue != null) {
                    if (queue.getRootElement().element("isfull").getText().equals("no")) {
                        for (int x = 1; x <= this.queueineed; x++) {
                            if (queue.getRootElement().element("number-" + x).getText().equals("empty")) {
                                queue.getRootElement().element("number-" + x).setText(Thread.currentThread().getId() + "");
                                isqueuing = true;
                                queueid = now;
                                log.printf("Joined Queue ID:" + now + " Name:" + queuename + " MemberNumber:" + queueineed, Thread.currentThread().getStackTrace()[1].getFileName(), Thread.currentThread().getStackTrace()[1].getLineNumber());
                                break;
                            }
                        }
                        for (int y = 1; y <= this.queueineed & !queue.getRootElement().element("number-" + y).getText().equals("empty"); y++)
                            if (y == this.queueineed)
                                queue.getRootElement().element("isfull").setText("yes");
                        if (!isqueuing) {
                            queue.getRootElement().element("isfull").setText("yes");
                            this.createqueue();
                        }
                    } else {
                        this.createqueue();
                    }
                    dataprocess.dom4j.unlock(queue, getQUEUEfile(now));
                } else {
                    this.createqueue();
                }
            } else {
                this.createqueue();
            }
        }
    }

    private void quit_queue() {
        if (isqueuing) {
            if (isfull())
                return;
            Document queue = dom4j.lock(getQUEUEfile(queueid));
            if (queue != null) {
                for (int x = 1; x <= this.queueineed; x++) {
                    if (queue.getRootElement().element("number-" + x).getText().equals(Thread.currentThread().getId() + "")) {
                        queue.getRootElement().element("number-" + x).setText("empty");
                        isqueuing = false;
                        break;
                    }
                }
                dom4j.unlock(queue, getQUEUEfile(queueid));
                log.printf("Quit Queue ID:" + queueid + " Name:" + queuename + " MemberNumber:" + queueineed, Thread.currentThread().getStackTrace()[1].getFileName(), Thread.currentThread().getStackTrace()[1].getLineNumber());
                queueid = 0;
                return;
            }
            isqueuing = false;
            queueid = 0;
        }
    }
    private boolean isfull() {
        if (isqueuing) {
            Document queue = dom4j.load(getQUEUEfile(queueid));
            return Objects.requireNonNull(queue).getRootElement().element("isfull").getText().equals("yes");
        }
        return false;
    }
    private Document getQueueInfo() {
        if (isqueuing) {
            return dom4j.load(getQUEUEfile(queueid));
        }
        return null;
    }
}