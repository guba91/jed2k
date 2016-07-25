package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import org.jed2k.exception.JED2KException;

public abstract class PacketCombiner {

    private static Logger log = Logger.getLogger(PacketCombiner.class.getName());

    public enum ProtocolType {
        OP_EDONKEYHEADER(0xE3),
        OP_EDONKEYPROT(0xE3),
        OP_PACKEDPROT(0xD4),
        OP_EMULEPROT(0xC5),
    	OP_KAD_COMPRESSED_UDP(0xE5);

        public final byte value;

        ProtocolType(int v) {
            this.value = (byte)v;
        }
    }

    enum KadUdp {
	    KADEMLIA_BOOTSTRAP_REQ		(0x00),
	    KADEMLIA_BOOTSTRAP_RES		(0x08),

	    KADEMLIA_HELLO_REQ			(0x10),
	    KADEMLIA_HELLO_RES			(0x18),

	    KADEMLIA_FIREWALLED_REQ		(0x50),
	    KADEMLIA_FIREWALLED_RES		(0x58),

	    KADEMLIA_CALLBACK_REQ		(0x52),

	    KADEMLIA_REQ				(0x20),
	    KADEMLIA_RES				(0x28),

	    KADEMLIA_PUBLISH_REQ		(0x40),
	    KADEMLIA_PUBLISH_RES		(0x48),

	    KADEMLIA_SEARCH_REQ			(0x30),
	    KADEMLIA_SEARCH_RES			(0x38),

	    KADEMLIA_SEARCH_NOTES_REQ	(0x32),
	    KADEMLIA_SEARCH_NOTES_RES	(0x3A),

	    KADEMLIA_FINDBUDDY_REQ		(0x51),
	    KADEMLIA_FINDBUDDY_RES		(0x5A),

	    KADEMLIA_PUBLISH_NOTES_REQ	(0x42),
	    KADEMLIA_PUBLISH_NOTES_RES	(0x4A),

	    KADEMLIA2_BOOTSTRAP_REQ		(0x01),
	    KADEMLIA2_BOOTSTRAP_RES		(0x09),

	    KADEMLIA2_REQ				(0x21),
	    KADEMLIA2_RES				(0x29),

	    KADEMLIA2_HELLO_REQ			(0x11),
	    KADEMLIA2_HELLO_RES 		(0x19),

	    KADEMLIA2_HELLO_RES_ACK		(0x22),

	    KADEMLIA_FIREWALLED2_REQ    (0x53),

	    KADEMLIA2_FIREWALLUDP		(0x62),

	    KADEMLIA2_SEARCH_KEY_REQ	(0x33),
	    KADEMLIA2_SEARCH_SOURCE_REQ	(0x34),
	    KADEMLIA2_SEARCH_NOTES_REQ	(0x35),

	    KADEMLIA2_SEARCH_RES		(0x3B),

	    KADEMLIA2_PUBLISH_KEY_REQ	(0x43),
	    KADEMLIA2_PUBLISH_SOURCE_REQ(0x44),
	    KADEMLIA2_PUBLISH_NOTES_REQ	(0x45),

	    KADEMLIA2_PUBLISH_RES		(0x4B),

	    KADEMLIA2_PUBLISH_RES_ACK	(0x4C),

	    KADEMLIA2_PING				(0x60),
	    KADEMLIA2_PONG				(0x61),

	    FIND_VALUE 					(0x02),
	    STORE      					(0x04),
	    FIND_NODE					(0x0B);

    	public final byte value;
    	private KadUdp(int v) {
    		value = (byte)v;
    	}
    }

    private PacketHeader outgoingHeader = new PacketHeader();

    /**
     *
     * @param header - packet header
     * @param src - byte buffer with raw data
     * @return serializable structure according with header
     * @throws JED2KException
     */
    public Serializable unpack(PacketHeader header, ByteBuffer src) throws JED2KException {
        assert(header.isDefined());
        assert(src.remaining() == serviceSize(header));

        PacketKey key = header.key();
        Class<? extends Serializable> clazz = keyToPacket(key);
        Serializable ph = null;

        if (clazz != null) {
            try {
                ph = clazz.newInstance();
            } catch(InstantiationException e) {
                throw new JED2KException(e);
            } catch (IllegalAccessException e) {
                throw new JED2KException(e);
            }
        } else {
            log.warning("unable to find correspond packet for " + header);
            ph = new BytesSkipper(serviceSize(header));
        }

        try {
            if (ph instanceof SoftSerializable) {
                SoftSerializable ssp = (SoftSerializable)ph;
                assert(ssp != null);
                ssp.get(src, serviceSize(header));
            } else {
                ph.get(src);
            }
        } catch(JED2KException e) {
            throw e;
        } catch(Exception e) {
            // catch any exception and convert it to our
            throw new JED2KException(e);
        }

        header.reset();
        return ph;

    }

    /**
     *
     * @param object - serializable structure
     * @param dst - destination byte buffer
     * @return true if buffer has enough space for packing
     * @throws JED2KException
     */
    public boolean pack(Serializable object, ByteBuffer dst) throws JED2KException {
        PacketKey key = classToKey(object.getClass());
        //log.info("pack for class " + object.getClass().getName() + " bytes " + object.bytesCount());
        assert key != null;
        if ((outgoingHeader.bytesCount() + object.bytesCount()) < dst.remaining()) {
            outgoingHeader.reset(key, object.bytesCount() + 1);
            //log.info(outgoingHeader.toString());
            assert(outgoingHeader.isDefined());
            outgoingHeader.put(dst);
            object.put(dst);
            return true;
        }

        return false;
    }

    protected abstract Class<? extends Serializable> keyToPacket(PacketKey key);
    protected abstract PacketKey classToKey(Class<? extends Serializable> clazz);
    public abstract int serviceSize(PacketHeader ph);
}