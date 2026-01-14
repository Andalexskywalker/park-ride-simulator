package pt.portfolio.prr.utilizadores.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(java.util.Map.of("message", ex.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        String debugMessage = "GLOBAL DEBUG ERROR: " + ex.getClass().getName() + " | Message: " + ex.getMessage()
                + "\nSTACK TRACE:\n" + sw.toString();

        System.err.println(debugMessage); // Force print to logs as well

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(debugMessage);
    }
}
