package com.hengtiansoft.strategy.model.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class BlobStringHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ByteArrayInputStream bis;
        bis = new ByteArrayInputStream(parameter.getBytes(StandardCharsets.UTF_8));
        ps.setBinaryStream(i, bis, parameter.length());
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Blob blob = rs.getBlob(columnName);
        return blobToString(blob);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Blob blob = rs.getBlob(columnIndex);
        return blobToString(blob);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Blob blob = cs.getBlob(columnIndex);
        return blobToString(blob);
    }

    private String blobToString(Blob blob) throws SQLException {
        byte[] returnValue = null;
        if (null != blob) {
            returnValue = blob.getBytes(1L, (int)blob.length());
        }

        return (returnValue==null)? null : new String(returnValue, StandardCharsets.UTF_8);
    }
}
