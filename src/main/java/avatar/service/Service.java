package avatar.service;

import avatar.constants.Cmd;
import avatar.network.Message;
import avatar.network.Session;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class Service {

    private static final Logger logger = Logger.getLogger(Service.class);
    protected Session session;

    public Service(Session cl) {
        this.session = cl;
    }

    public void removeItem(int userID, short itemID) {
        try {
            Message ms = new Message(Cmd.REMOVE_ITEM);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeShort(itemID);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("removeItem() ", ex);
        }


    }

    public void serverDialog(String message) {
        try {
            Message ms = new Message(Cmd.SET_MONEY_ERROR);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTextBoxPopup(int userId, int menuId, String message, int type) {
        try {
            Message ms = new Message(Cmd.TEXT_BOX);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userId);
            ds.writeByte(menuId);
            ds.writeUTF(message);
            ds.writeByte(type);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverMessage(String message) {
        try {
            Message ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("serverMessage ", e);
        }
    }

    public void serverInfo(String message) {
        try {
            Message ms = new Message(Cmd.SERVER_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("serverMessage ", e);
        }
    }

    public void weather(byte weather) {
        try {
            System.out.println("weather: " + weather);
            Message ms = new Message(Cmd.WEATHER);
            DataOutputStream ds = ms.writer();
            ds.writeByte(weather);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("weather() ", ex);
        }
    }

    public void sendMessage(Message ms) {
        session.sendMessage(ms);
    }
}
