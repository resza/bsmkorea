package org.jpos.space;

import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.jpos.iso.ISOMsg;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ReplicatedSpaceTestCase extends TestCase {
    ReplicatedSpace rs1;
    ReplicatedSpace rs2;
    Logger logger;
    public void setUp() throws Exception {
        logger = new Logger();
        logger.addListener (new SimpleLogListener (System.out));
        rs1 = new ReplicatedSpace (
            SpaceFactory.getSpace ("tspace:sp1"),
            "rspace", "../cfg/udp.xml", logger, "RS1", true, true
        );
    }

    public void testAll () throws Exception {
        outISOMsg ();
        rs2 = new ReplicatedSpace (
            SpaceFactory.getSpace ("tspace:sp2"),
            "rspace", "../cfg/udp.xml", logger, "RS2", true, true
        );
        inISOMsg();
        for (int i=0; i<10; i++) {
            basicTest ();
            outInRs1();
            outInRs2();
            outInRs1AndRs2();
            pushInBoth();
            outRdIn ("out-rd-in-0", rs1, rs1);
            outRdIn ("out-rd-in-0", rs2, rs2);
            outRdIn ("out-rd-in-0", rs1, rs2);
            outRdIn ("out-rd-in-0", rs2, rs1);
            emptyCheck();
        }
    }
    public void outISOMsg() throws Exception {
        ISOMsg m = new ISOMsg ("0800");
        m.set (3, "000000");
        m.set (11, "000001");
        m.set (41, "29110001");
        m.set (70, "301");
        rs1.out ("ISOQUEUE", m);
    }
    public void inISOMsg() throws Exception {

        // verify that the message is in the local sp2
        Space sp = SpaceFactory.getSpace ("tspace:sp2");
        ISOMsg m = (ISOMsg) sp.rd ("ISOQUEUE", 5000L);
        if (m != null)
            m.dump (System.out, "local-sp2> ");
        assertNotNull ("ISOMsg is null in local sp2", m);

        m = (ISOMsg) rs2.inp ("ISOQUEUE");
        if (m != null)
            m.dump (System.out, "");
        assertNotNull ("ISOMsg is null", m);
    }
    public void basicTest () throws Exception {
        rs2.out ("Test", "rs1-0", 10000L);
        assertEquals ("rs1-0", rs1.rd ("Test"));
        assertEquals ("rs1-0", rs2.rd ("Test"));

        assertEquals ("rs1-0", rs1.in ("Test"));
        rs2.out ("Test", "rs2-0", 10000L);
        assertEquals ("rs2-0", rs1.rd ("Test"));
        assertEquals ("rs2-0", rs2.rd ("Test"));
        assertEquals ("rs2-0", rs2.in ("Test"));
    }
    public void outRdIn (String key, Space s1, Space s2) throws Exception {
        s1.out (key, "0", 10000L);
        assertEquals ("0", s1.rd (key, 1000));
        assertEquals ("0", s2.rd (key, 1000));

        s2.out (key, "1", 10000L);
        assertEquals ("0", s1.rd (key, 1000));
        assertEquals ("0", s2.rd (key, 1000));

        // in in s1
        assertEquals ("0", s1.in (key, 1000));
        assertEquals ("1", s1.rd (key, 1000));
        assertEquals ("1", s2.rd (key, 1000));

        // in in s2
        assertEquals ("1", s2.in (key, 1000));
    }
    public void outInRs1() throws Exception {
        rs1.out ("Test", "Test on RS1", 10000L);
        assertEquals ("Test on RS1", rs1.rd ("Test", 1000));
        assertEquals ("Test on RS1", rs2.rd ("Test", 1000));
        assertEquals ("Test on RS1", rs2.in ("Test", 1000));
    }
    public void outInRs2() throws Exception {
        rs2.out ("Test", "Test on RS2", 10000L);
        assertEquals ("Test on RS2", rs1.rd ("Test", 1000));
        assertEquals ("Test on RS2", rs2.rd ("Test", 1000));
        assertEquals ("Test on RS2", rs1.in ("Test", 1000));
    }
    public void outInRs1AndRs2() throws Exception {
        rs1.out ("Test", "Test 2 on RS1", 10000L);
        rs2.out ("Test", "Test 2 on RS2", 10000L);
        assertEquals ("Test 2 on RS1", rs1.rd ("Test", 1000));
        assertEquals ("Test 2 on RS1", rs2.rd ("Test", 1000));
        assertEquals ("Test 2 on RS1", rs2.in ("Test", 1000));
        assertEquals ("Test 2 on RS2", rs1.rd ("Test", 1000));
        assertEquals ("Test 2 on RS2", rs2.rd ("Test", 1000));
        assertEquals ("Test 2 on RS2", rs1.in ("Test", 1000));
    }

    public void pushInBoth () throws Exception {
        rs1.push ("Test-push", "Test PUSH RS1", 10000L);
        assertEquals ("Test PUSH RS1", rs1.rd ("Test-push", 1000));
        assertEquals ("Test PUSH RS1", rs2.rd ("Test-push", 1000));
        rs1.push ("Test-push", "Test PUSH RS1.1", 10000L);
        assertEquals ("Test PUSH RS1.1", rs1.rd ("Test-push", 1000));
        assertEquals ("Test PUSH RS1.1", rs2.rd ("Test-push", 1000));
        assertEquals ("Test PUSH RS1.1", rs1.in ("Test-push", 1000));

        rs2.push ("Test-push", "Test PUSH RS2", 10000L);
        assertEquals ("Test PUSH RS2", rs1.rd ("Test-push", 1000));
        assertEquals ("Test PUSH RS2", rs2.rd ("Test-push", 1000));
        assertEquals ("Test PUSH RS2", rs1.in ("Test-push", 1000));
        assertEquals ("Test PUSH RS1", rs2.in ("Test-push", 1000));
    }
    public void emptyCheck() throws Exception {
        assertNull (rs1.inp ("Test"));
        assertNull (rs2.inp ("Test"));
        assertNull (rs1.rdp ("Test"));
        assertNull (rs2.rdp ("Test"));
        assertNull (rs1.rdp ("Test-push"));
        assertNull (rs2.rdp ("Test-push"));
    }
    public void tearDown() throws Exception {
        rs1.close();
        rs2.close();
    }
}

