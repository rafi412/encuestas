package main;

import collections.Respuesta;
import org.bson.*;
import org.bson.codecs.*;

public class RespuestaItemCodec implements Codec<Respuesta.RespuestaItem> {

    @Override
    public Respuesta.RespuestaItem decode(BsonReader reader, DecoderContext decoderContext) {
        BsonDocument document = BsonDocument.parse(reader.readString()); // Método alternativo
        int idPregunta = document.getInt32("idPregunta").getValue();
        String respuesta = document.getString("respuesta").getValue();
        return new Respuesta.RespuestaItem(idPregunta, respuesta);
    }

    @Override
    public void encode(BsonWriter writer, Respuesta.RespuestaItem value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeInt32("idPregunta", value.getIdPregunta());
        writer.writeString("respuesta", value.getRespuesta());
        writer.writeEndDocument();
    }

    @Override
    public Class<Respuesta.RespuestaItem> getEncoderClass() {
        return Respuesta.RespuestaItem.class;
    }
}
