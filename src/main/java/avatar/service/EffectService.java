package avatar.service;

import avatar.constants.Cmd;
import avatar.model.Position;
import avatar.network.Message;
import avatar.network.Session;
import lombok.Builder;

import java.io.DataOutputStream;
import java.io.IOException;

public class EffectService {
    @Builder(builderMethodName = "createEffect", buildMethodName = "send")
    private static void sendEffect(Session session, byte id, byte style, byte loopLimit, short num, byte timeStop, short loop, byte loopType, short radius, Position[] positions, int idPlayer, Position position) {
        try {
            if (session == null) {
                //System.err.println("Session is null, cannot send effect.");
                return;
            }

            Message ms = new Message(Cmd.EFFECT_OBJ);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(id);
            ds.writeByte(style);
            ds.writeByte(loopLimit);

            if (style == 4) {
                ds.writeShort(num);
                ds.writeByte(timeStop);
            } else {
                ds.writeShort(loop);
                ds.writeByte(loopType);
                if (loopType == 1) {
                    ds.writeShort(radius);
                } else if (loopType == 2) {
                    if (positions == null) {
                        System.err.println("Positions array is null, cannot write positions.");
                        return;
                    }
                    ds.writeByte(positions.length);
                    for (Position p : positions) {
                        if (p == null) {
                            System.err.println("Position in positions array is null, cannot write position.");
                            continue;
                        }
                        ds.writeShort(p.getX());
                        ds.writeShort(p.getY());
                    }
                }
                if (style == 0) {
                    ds.writeInt(idPlayer);
                } else {
                    ds.writeShort(position.getX());
                    ds.writeShort(position.getY());
                }
            }

            ds.flush();
            session.sendMessage(ms);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
