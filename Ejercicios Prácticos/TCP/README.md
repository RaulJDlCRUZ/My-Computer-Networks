# Ejercicios de Sockets TCP

## Ejercicio 1

Crea un programa que muestre el nombre de tu host y una lista de las direcciones IPs de tus adaptadores de red:

<ul>
<li>Utiliza el método estático <code>getLocalHost</code> de <code>InetAddress</code> para obtener el objeto localhost</li>
<li>Utiliza el método <code>getAllByName</code> pasando como parámetro el nombre canónico del localhost (getCanonicalHostName)</li>
<li>Recorre el array que devuelve</li>
</ul>

```console
Lista de mis direcciones IP:
    DESKTOP-8SJPEB2/172.16.1.2
    DESKTOP-8SJPEB2/192.168.1.112
    DESKTOP-8SJPEB2/192.168.56.1
    DESKTOP-8SJPEB2/fe80:0:0:0:29a6:6196:1792:6deb%54
    DESKTOP-8SJPEB2/fe80:0:0:0:a1c4:82c6:2360:8429%21
    DESKTOP-8SJPEB2/fe80:0:0:0:155:c766:3251:cd7a%13
```

## Ejercicio 2

Crea un programa que muestre los adaptadores de red que tengan conexión y las direcciones IP de cada uno de ellos (tanto IPv4 como IPv6)

<ul>
<li>Recorre la enumeración que devuelve <code>NetworkInterface.getNetworkInterfaces()</code></li>
<li>Utiliza el método <code>isUp()</code> para comprobar que tenga conexión</li>
<li>Por cada interfaz, recorre el número de direcciones IP que devuelve el método <code>getInetAddresses</code> de cada interfaz</li>
</ul>

```console
Listado de interfaces de red:
    lo Software Loopback Interface 1
        /127.0.0.1
        /0:0:0:0:0:0:0:1
    net5 TAP-Windows Adapter V9
        /172.16.1.2
        /fe80:0:0:0:155:c766:3251:cd7a%net5
    eth6 Realtek PCIe GbE Family Controller
        /192.168.1.112
        /fe80:0:0:0:a1c4:82c6:2360:8429%eth6
    eth17 VirtualBox Host-Only Ethernet Adapter
        /192.168.56.1
        /fe80:0:0:0:29a6:6196:1792:6deb%eth17
```

### Retoca el ejercicio anterior para que muestre sólo aquellas direcciones IP que sean versión 4

```
if (address instanceof Inet4Address)
```


## Ejercicio 3

Escribe una palabra y lee de un servidor de echo. Los parámetros del programa de la línea de argumentos serán

```console
Parámetros: <Servidor> <palabra> [<puerto>]
```

1. Abre el socket al puerto del servidor
2. Captura los streams de entrada y salida
3. Envía la palabra a través del output stream
4. Recibe bytes hasta que hayas recibido de vuelta toda la palabra o se el servidor cierre la conexión

> ¿Tal vez `sudo apt-get install openbsd-inetd` y `vi /etc/inetd.conf`?

### Consideraciones

¿Por qué no una única lectura?
1. `read()` bloquea la ejecución del hilo hasta que hay datos disponibles y retorna el número de bytes que se depositan en el buffer
2. El bucle rellena el array hasta que se reciben los bytes esperados.
    - Si la conexión se cierra en el otro extremo, `read()` se desbloqueará 
3. Es posible que TCP no respete la longitud del mensaje indicado en write() y que se envíe el mensaje en varios trozos (varios segmentos TCP)
4. Incluso aunque el segmento se envíe en un solo trozo y el servidor lo reciba en un único segmento, la respuesta puede producir varios segmentos

**No podemos asumir que lo que enviamos con un `write`, se envíe en un solo segmento y los que queremos recibir en un read, llegue en un solo segmento**

## Ejercicio 4

Implementa un servidor echo.

```console
Parámetros: <puerto servidor>
```

1. Abre el socket servidor
2. Bucle infinito 
    - Acepta conexión
    - Lee del InputStream
    - Escribe en el OutputStream

## Ejercicio 5

Crea el servidor **`CaseChanger`**, que cambia las mayúsculas porminúsculas y las minúsculas por mayúsculas.

> De este hay dos versiones: una versión [normal](./Ejercicio5/CaseChanger.java) y otra [multihilo](./Ejercicio5/CaseChangerMultihilo.java)

Crea un cliente para comunicarte con **CaseChanger**: El cliente aceptará líneas de texto por pantalla hasta que el usuario introduzca la palabra _"FIN"_

## Ejercicio 6

Crea un servidor de ECHO multihilo utilizando un manejador que herede de la clase `Thread`

## Ejercicio 7

Crea un programa que utilice sockets para crear un servidor _http_ que admita únicamente el método **GET**
> `GET url HTTP/1.1`

```console
Parámetros: <puerto servidor> <ruta_raíz>
```

donde **&lt;ruta_raíz&gt;** es la carpeta donde los archivos HTML están (webserver root - wwwroot)

> Probar con recursos webyoga!