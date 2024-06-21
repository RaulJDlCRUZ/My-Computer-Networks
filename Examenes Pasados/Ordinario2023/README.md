# EXAMEN DE REDES DE COMPUTADORES 2 – 22 de mayo de 2023

## TCP - Cadena enmarañada

Se puede generar una cadena enmarañada (scrambled) a partir de una cadena delongitud n de la siguiente forma: Si la longitud de la cadena es <=1, parar; si la longitud de la cadena es >1, hacer lo siguiente:

<ul>
<li>Divide la cadena en dos partes por un índice aleatorio, por ejemplo, si
la cadena es S, divídela en X e Y de manera que S=X+Y</li>
<li>Intercambia el orden de las dos subcadenas formando S=Y+X</li>
<li>Aplicar el primer paso recursivamente para cada una de las dos
subcadenas</li>
</ul>

Al final del día, lo que logramos con esto es invertir la palabra.

Crear un servidor TCP multihilo en el puerto 1721 que reciba cadenas de caracteres de longitud $n$ (siendo $n$ de una longitud menor de 200) y que devuelva palabras enmarañadas de esas cadenas hasta que se reciba la palabra `FIN`. En cuanto reciba la palabra `FIN`, cerrará la conexión con el cliente devolviendo la palabra _"`NIF`"_, y no se procesarán las que quedasen en esa misma línea.

## UDP - Fórmula 1

Necesitamos crear un programa que lea los datos de un sensor acelerómetro de un coche de fórmula 1 y los envíe a un servidor que realizará una estadística de la aceleración del vehículo durante un periodo de tiempo. El sensor tiene una API para poder realizar las lecturas de forma muy sencilla utilizando la clase Sensor y su método estático leer, que devuelve bytes con la fuerza ejercida en los ejes X, Y, Z:

<pre>

while(true){
    X=Sensor.leer();
    Y=Sensor.leer();
    Z=Sensor.leer();
    System.out.println("fuerza (x,y,z): "+X+" "+Y+" "+Z);
}</pre>

Cada invocación al método leer devuelve un byte con un valor entre 0 y 255. La primera lectura, corresponderá a la fuerza ejercida sobre el eje X, la segunda lectura sobre la fuerza ejercida sobre el eje Y y la tercera sobre la fuerza ejercida sobre el eje Z. Cada lectura completa (coordenadas ejes X,Y,Z) se enviará con un datagrama UDP al servidor UDP está ubicado en la dirección sensores.uclm.es en el puerto 9999.

El servidor no devuelve ningún dato, tan sólo recibe y procesa las medidas.

Crea un programa en Java que lea los datos de temperatura y que, cada 3 bytes leídos, forme un datagrama y lo envíe al servidor UDP indicado.
