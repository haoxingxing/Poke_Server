package process;

import dataprocess.dom4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class matchqueue {
    private user u;
    private boolean isqueuing = false;
    private String queuename;
    private int queueineed;

    public matchqueue(user u, String queuename, int queueinneed) {
        this.u = u;
        this.queuename = queuename;
        this.queueineed = queueinneed;
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
        Document config = dom4j.load(getCONFIGfile());
        AtomicInteger nowqueuecreated = new AtomicInteger();
        if (config != null) {
            while (Objects.requireNonNull(config).getRootElement().element("lock").getText().equals("lock")) {
                config = dom4j.load(getCONFIGfile());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            config.getRootElement().element("lock").setText("lock");
            dataprocess.dom4j.write(config, getCONFIGfile());
            config.getRootElement().element("itor").setText((Integer.parseInt(config.getRootElement().element("itor").getText()) + 1) + "");
            nowqueuecreated.set(Integer.parseInt(config.getRootElement().element("itor").getText()));
            config.getRootElement().element("lock").setText("unlock");
        } else {
            config = DocumentHelper.createDocument();
            Element root = config.addElement("queuecfg");
            root.addElement("itor").setText("1");
            root.addElement("lock").setText("unlock");
            nowqueuecreated.set(1);
        }
        dataprocess.dom4j.write(config, getCONFIGfile());
        int nowqueueid = nowqueuecreated.get();
        Document newqueue = DocumentHelper.createDocument();
        Element rootofqueue = newqueue.addElement("queue");
        for (AtomicInteger x = new AtomicInteger(1); x.get() <= this.queueineed; x.getAndIncrement())
            rootofqueue.addElement("number-" + x.get()).setText("");
        rootofqueue.addElement("isfull").setText("no");
        rootofqueue.addElement("lock").setText("unlock");
        dataprocess.dom4j.write(newqueue, getQUEUEfile(nowqueueid));
    }

    public void join() {
        while (!isqueuing) {
            Document doc = dom4j.load(getCONFIGfile());
            if (doc != null) {
                while (Objects.requireNonNull(doc).getRootElement().element("lock").getText().equals("lock")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    doc = dom4j.load(getCONFIGfile());
                }
                int now = Integer.parseInt(doc.getRootElement().element("itor").getText());
                Document queue = dom4j.load(getQUEUEfile(now));
                if (queue != null) {
                    while (Objects.requireNonNull(queue).getRootElement().element("lock").getText().equals("lock")) {
                        queue = dataprocess.dom4j.load(getQUEUEfile(now));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    queue.getRootElement().element("lock").setText("lock");
                    dataprocess.dom4j.write(queue, getQUEUEfile(now));
                    if (queue.getRootElement().element("isfull").getText().equals("no")) {
                        for (int x = 1; x <= this.queueineed; x++) {
                            if (queue.getRootElement().element("number-" + x).getText().equals("")) {
                                queue.getRootElement().element("number-" + x).setText(u.username);
                                isqueuing = true;
                                if (x == this.queueineed) {
                                    queue.getRootElement().element("isfull").setText("yes");
                                }
                                break;
                            }
                        }
                        if (!isqueuing) {
                            queue.getRootElement().element("isfull").setText("yes");
                            this.createqueue();
                        }
                    } else {
                        this.createqueue();
                    }
                    queue.getRootElement().element("lock").setText("unlock");
                    dataprocess.dom4j.write(queue, getQUEUEfile(now));
                } else {
                    this.createqueue();
                }
            } else {
                this.createqueue();
            }
        }
    }
}