package com.csmp.supply.support;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Tag("dev")
class PostgresJsonbStringTypeHandlerTest {

    @Test
    void setNonNullParameterShouldBindJsonbPgObject() throws SQLException {
        PostgresJsonbStringTypeHandler handler = new PostgresJsonbStringTypeHandler();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        handler.setNonNullParameter(preparedStatement, 3, "{\"cpu\":16}", null);

        ArgumentCaptor<PGobject> captor = ArgumentCaptor.forClass(PGobject.class);
        verify(preparedStatement).setObject(anyInt(), captor.capture());
        PGobject actual = captor.getValue();
        assertEquals("jsonb", actual.getType());
        assertEquals("{\"cpu\":16}", actual.getValue());
    }

    @Test
    void setNonNullParameterShouldBindNullWhenBlank() throws SQLException {
        PostgresJsonbStringTypeHandler handler = new PostgresJsonbStringTypeHandler();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        handler.setNonNullParameter(preparedStatement, 13, "   ", null);

        verify(preparedStatement).setNull(13, Types.OTHER);
    }
}
