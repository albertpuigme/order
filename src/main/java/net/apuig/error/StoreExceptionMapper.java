package net.apuig.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class StoreExceptionMapper
{
    private static final Logger LOG = LoggerFactory.getLogger(StoreExceptionMapper.class);

    @ResponseBody
    @ExceptionHandler(StoreWebException.class)
    public ResponseEntity<ErrorDto> storeException(final StoreWebException ex)
    {
        return new ResponseEntity<>(new ErrorDto(ex.getClass().getSimpleName(), ex.getMessage()),
            ex.getStatus());
    }

    @ResponseBody
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDto> dataAccess(final DataAccessException ex)
    {
        LOG.error("DataAccessException", ex);
        // TODO instanceof DataIntegrityViolationException to know if its safe to retry
        return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.RETRY_AFTER, "1")
            .body(new ErrorDto("DataAccessException",
                "An unexpected error has occurred, please try again"));
    }
}
