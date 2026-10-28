package com.utn.inventario.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** JwtService sin Spring: la clave y la expiracion se cargan a mano. */
class JwtServiceTest {

    private static final String CLAVE = "clave-de-prueba-de-al-menos-treinta-y-dos-bytes-1234567890";
    private static final String OTRA_CLAVE = "otra-clave-distinta-tambien-de-mas-de-treinta-y-dos-bytes-xyz";

    private JwtService jwtService;
    private UserDetails ana;

    private static JwtService crearServicio(String clave, long expiracionMs) {
        JwtService servicio = new JwtService();
        ReflectionTestUtils.setField(servicio, "jwtSecret", clave);
        ReflectionTestUtils.setField(servicio, "jwtExpirationMs", expiracionMs);
        return servicio;
    }

    @BeforeEach
    void setUp() {
        jwtService = crearServicio(CLAVE, 60_000);
        ana = User.withUsername("ana@inventario.com").password("x").roles("EMPLEADO").build();
    }

    @Test
    void unTokenGeneradoEsValidoParaSuUsuario() {
        // Arrange
        String token = jwtService.generarToken(ana);

        // Act
        boolean valido = jwtService.esTokenValido(token, ana);

        // Assert
        assertThat(valido).isTrue();
    }

    @Test
    void extraerEmailDevuelveElEmailUsadoAlGenerarlo() {
        // Arrange
        String token = jwtService.generarToken(ana);

        // Act
        String email = jwtService.extraerEmail(token);

        // Assert
        assertThat(email).isEqualTo("ana@inventario.com");
    }

    @Test
    void unTokenNoEsValidoParaOtroUsuario() {
        // Arrange
        String token = jwtService.generarToken(ana);
        UserDetails beto = User.withUsername("beto@inventario.com").password("x").roles("EMPLEADO").build();

        // Act
        boolean valido = jwtService.esTokenValido(token, beto);

        // Assert
        assertThat(valido).isFalse();
    }

    @Test
    void unTokenManipuladoNoValida() {
        // Arrange: se cambia el contenido (payload) sin volver a firmar.
        String[] partes = jwtService.generarToken(ana).split("\\.");
        String payloadFalso = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"admin@inventario.com\"}".getBytes());
        String manipulado = partes[0] + "." + payloadFalso + "." + partes[2];

        // Act + Assert
        assertThatThrownBy(() -> jwtService.esTokenValido(manipulado, ana))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void unTokenFirmadoConOtraClaveNoValida() {
        // Arrange
        String tokenAjeno = crearServicio(OTRA_CLAVE, 60_000).generarToken(ana);

        // Act + Assert
        assertThatThrownBy(() -> jwtService.esTokenValido(tokenAjeno, ana))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void unTokenVencidoNoValida() {
        // Arrange: expiracion negativa = el token nace vencido.
        String vencido = crearServicio(CLAVE, -1_000).generarToken(ana);

        // Act + Assert
        assertThatThrownBy(() -> jwtService.esTokenValido(vencido, ana))
                .isInstanceOf(JwtException.class);
    }
}
