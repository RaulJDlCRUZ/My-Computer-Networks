# Ejercicios de Sockets UDP y SSL/TLS

## Ejercicio 1

Construye un servidor de echo y un cliente UDP:

<ul>
    <li><b>Cliente</b><br>
        <ul>
            <li>Lee de la línea de argumentos el servidor y puerto</li>
            <li>Lee de teclado hasta que el usuario introduzca FIN</li>
            <li>Máx reintentos: 5</li>
            <li>Timeout: 3 segundos</li>
        </ul>
    </li>
    <li><b>Servidor</b><br>
        <ul>
        <li>Lee de la línea de argumentos el puerto donde debe escuchar</li>
        <li>Cadena más grande a recibir 255</li>
        </ul>
    </li>
</ul>

## Ejercicio 2

**Servidor de echo SSL**. Crea una aplicación cliente-servidor de echo utilizando Sockets SSL:

<ul>
<li>El servidor escucha en el puerto 1721</li>
<li>Argumentos:
    <ul>
        <li>La ruta a la carpeta de certificados</li>
    </ul>
</li>
<li>Utilizar modo normal, es decir, sólo el servidor envía el cerfificado
    <ul>
        <li><i>ONE-WAY HANDSHAKE</i></li>
    </ul>
</li>
</ul>

## Ejercicio 3

**Lo mismo que el [Ejercicio 2](#ejercicio-2) pero con _Two-Way Handshake_**, es decir, tanto cliente como servidor intercambian sus certificados para poder conectarse.