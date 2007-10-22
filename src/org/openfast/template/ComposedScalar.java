package org.openfast.template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.openfast.BitVectorBuilder;
import org.openfast.BitVectorReader;
import org.openfast.Context;
import org.openfast.FieldValue;
import org.openfast.QName;
import org.openfast.template.type.Type;

public class ComposedScalar extends Field {
	private static final long serialVersionUID = 1L;
	private static final Class ScalarValueType = null;
	private Scalar[] fields;
	private ComposedValueConverter valueConverter;
	private Type type;

	public ComposedScalar(String name, Type type, Scalar[] fields, boolean optional, ComposedValueConverter valueConverter) {
		this(new QName(name), type, fields, optional, valueConverter);
	}
	
	public ComposedScalar(QName name, Type type, Scalar[] fields, boolean optional, ComposedValueConverter valueConverter) {
		super(name, optional);
		this.fields = fields;
		this.valueConverter = valueConverter;
		this.type = type;
	}
	
	public FieldValue createValue(String value) {
		return type.getValue(value);
	}

	public FieldValue decode(InputStream in, Group template, Context context, BitVectorReader presenceMapReader) {
		FieldValue[] values = new FieldValue[fields.length];
		for (int i=0; i<fields.length; i++) {
			values[i] = fields[i].decode(in, template, context, presenceMapReader);
		}
		return valueConverter.compose(values);
	}

	public byte[] encode(FieldValue value, Group template, Context context, BitVectorBuilder presenceMapBuilder) {
		if (value == null) {
			// Only encode null in the first field.
			return fields[0].encode(null, template, context, presenceMapBuilder);
		} else {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(fields.length * 8);
			FieldValue[] values = valueConverter.split(value);
			for (int i=0; i<fields.length; i++) {
				try {
					buffer.write(fields[i].encode(values[i], template, context, presenceMapBuilder));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return buffer.toByteArray();
		}
	}

	public String getTypeName() {
		return type.getName();
	}

	public Class getValueType() {
		return ScalarValueType;
	}

	public boolean isPresenceMapBitSet(byte[] encoding, FieldValue fieldValue) {
		return false;
	}

	public boolean usesPresenceMapBit() {
		return false;
	}

	public Type getType() {
		return type;
	}

	public Scalar[] getFields() {
		return fields;
	}
	
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || !obj.getClass().equals(ComposedScalar.class)) return false;
		ComposedScalar other = (ComposedScalar) obj;
		if (other.fields.length != fields.length) return false;
		if (!other.getName().equals(getName())) return false;
		for (int i=0; i<fields.length; i++) {
			if (!other.fields[i].getType().equals(fields[i].getType())) return false;
			if (!other.fields[i].getTypeCodec().equals(fields[i].getTypeCodec())) return false;
			if (!other.fields[i].getOperator().equals(fields[i].getOperator())) return false;
			if (!other.fields[i].getOperatorCodec().equals(fields[i].getOperatorCodec())) return false;
			if (!other.fields[i].getInitialValue().equals(fields[i].getInitialValue())) return false;
			if (!other.fields[i].getDictionary().equals(fields[i].getDictionary())) return false;
		}
		return true;
	}
	
	public int hashCode() {
		return name.hashCode();
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Composed {");
		for (int i=0; i< fields.length; i++)
			builder.append(fields[i].toString()).append(", ");
		builder.delete(builder.length()-2, builder.length());
		return builder.append("}").toString();
	}

}