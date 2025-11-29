#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_ESTADOS 20
#define MAX_SIMBOLOS 10
#define MAX_NOMBRE 10
#define MAX_LINEA 256

// Estructura para representar una transición
typedef struct Transicion
{
    int destino;
    char simbolo;
    struct Transicion *siguiente;
} Transicion;

// Estructura para representar un estado (nodo del grafo)
typedef struct Estado
{
    char nombre[MAX_NOMBRE];
    int id;
    int es_inicial;
    int es_final;
    Transicion *transiciones;
} Estado;

// Estructura para representar el autómata finito determinista
typedef struct AFD
{
    Estado estados[MAX_ESTADOS];
    char alfabeto[MAX_SIMBOLOS];
    int num_estados;
    int num_simbolos;
    int estado_inicial;
} AFD;

// Función para agregar una transición a un estado
void agregar_transicion(Estado *estado, int destino, char simbolo)
{
    Transicion *nueva = (Transicion *)malloc(sizeof(Transicion));
    nueva->destino = destino;
    nueva->simbolo = simbolo;
    nueva->siguiente = estado->transiciones;
    estado->transiciones = nueva;
}

// Función para encontrar el índice de un estado por su nombre
int encontrar_estado(AFD *afd, const char *nombre)
{
    for (int i = 0; i < afd->num_estados; i++)
    {
        if (strcmp(afd->estados[i].nombre, nombre) == 0)
        {
            return i;
        }
    }
    return -1;
}

// Función para leer el AFD desde un archivo CSV
AFD *leer_afd_csv(const char *nombre_archivo)
{
    FILE *archivo = fopen(nombre_archivo, "r");
    if (!archivo)
    {
        printf("Error: No se pudo abrir el archivo %s\n", nombre_archivo);
        return NULL;
    }

    AFD *afd = (AFD *)malloc(sizeof(AFD));
    afd->num_estados = 0;
    afd->num_simbolos = 0;
    afd->estado_inicial = 0;

    // Inicializar todos los estados
    for (int i = 0; i < MAX_ESTADOS; i++)
    {
        afd->estados[i].transiciones = NULL;
        afd->estados[i].es_inicial = 0;
        afd->estados[i].es_final = 0;
    }

    char linea[MAX_LINEA];
    char estados_temp[MAX_ESTADOS][MAX_NOMBRE];
    char transiciones_temp[MAX_ESTADOS][MAX_SIMBOLOS][MAX_NOMBRE];

    // Leer primera línea (alfabeto)
    if (fgets(linea, sizeof(linea), archivo))
    {
        char *token = strtok(linea, ",\n\r");
        token = strtok(NULL, ",\n\r"); // Saltar primera columna vacía

        while (token && afd->num_simbolos < MAX_SIMBOLOS)
        {
            afd->alfabeto[afd->num_simbolos] = token[0];
            afd->num_simbolos++;
            token = strtok(NULL, ",\n\r");
        }
    }

    // Primera pasada: leer nombres de estados
    while (fgets(linea, sizeof(linea), archivo) && afd->num_estados < MAX_ESTADOS)
    {
        char *token = strtok(linea, ",\n\r");
        if (!token || strlen(token) == 0)
            continue;

        strcpy(estados_temp[afd->num_estados], token);
        strcpy(afd->estados[afd->num_estados].nombre, token);
        afd->estados[afd->num_estados].id = afd->num_estados;

        // Verificar si es estado inicial (contiene +)
        int len = strlen(token);
        if (strchr(token, '+') != NULL)
        {
            afd->estados[afd->num_estados].es_inicial = 1;
            afd->estado_inicial = afd->num_estados;
        }

        // Verificar si es estado de aceptación (contiene *)
        if (strchr(token, '*') != NULL)
        {
            afd->estados[afd->num_estados].es_final = 1;
        }

        // Leer transiciones temporalmente
        for (int j = 0; j < afd->num_simbolos; j++)
        {
            token = strtok(NULL, ",\n\r");
            if (token && strlen(token) > 0)
            {
                strcpy(transiciones_temp[afd->num_estados][j], token);
            }
            else
            {
                strcpy(transiciones_temp[afd->num_estados][j], "");
            }
        }

        afd->num_estados++;
    }

    // Segunda pasada: crear transiciones con índices correctos
    for (int i = 0; i < afd->num_estados; i++)
    {
        for (int j = 0; j < afd->num_simbolos; j++)
        {
            if (strlen(transiciones_temp[i][j]) > 0)
            {
                int destino = encontrar_estado(afd, transiciones_temp[i][j]);

                // Si no se encuentra el estado exacto, buscar por sufijo (sin prefijos i_ o a_)
                if (destino == -1)
                {
                    char nombre_sin_prefijo[MAX_NOMBRE];
                    strcpy(nombre_sin_prefijo, transiciones_temp[i][j]);

                    // Buscar estado que termine con este nombre
                    for (int k = 0; k < afd->num_estados; k++)
                    {
                        // Comparar con el nombre completo
                        if (strcmp(afd->estados[k].nombre, nombre_sin_prefijo) == 0)
                        {
                            destino = k;
                            break;
                        }

                        // Comparar ignorando sufijos + y *
                        char nombre_estado_temp[MAX_NOMBRE];
                        strcpy(nombre_estado_temp, afd->estados[k].nombre);
                        int len_estado = strlen(nombre_estado_temp);

                        // Remover todos los sufijos + y *
                        for (int idx = len_estado - 1; idx >= 0; idx--)
                        {
                            if (nombre_estado_temp[idx] == '+' || nombre_estado_temp[idx] == '*')
                            {
                                nombre_estado_temp[idx] = '\0';
                            }
                            else
                            {
                                break;
                            }
                        }

                        if (strcmp(nombre_estado_temp, nombre_sin_prefijo) == 0)
                        {
                            destino = k;
                            break;
                        }
                    }
                }

                if (destino != -1)
                {
                    agregar_transicion(&afd->estados[i], destino, afd->alfabeto[j]);
                }
            }
        }
    }

    fclose(archivo);
    return afd;
}

// Función para mostrar el autómata
void mostrar_automata(AFD *afd)
{
    printf("\n=== AUTOMATA FINITO DETERMINISTA ===\n");
    printf("Alfabeto: {");
    for (int i = 0; i < afd->num_simbolos; i++)
    {
        printf("%c", afd->alfabeto[i]);
        if (i < afd->num_simbolos - 1)
            printf(", ");
    }
    printf("}\n\n");

    printf("Estados y Transiciones:\n");
    for (int i = 0; i < afd->num_estados; i++)
    {
        printf("Estado %s", afd->estados[i].nombre);
        if (afd->estados[i].es_inicial)
            printf(" (inicial)");
        if (afd->estados[i].es_final)
            printf(" (aceptacion)");
        printf(":\n");

        Transicion *t = afd->estados[i].transiciones;
        while (t)
        {
            printf("d(%s, %c) = %s\n",
                   afd->estados[i].nombre,
                   t->simbolo,
                   afd->estados[t->destino].nombre);
            t = t->siguiente;
        }
        printf("\n");
    }
}

// Función para encontrar la transición de un estado con un símbolo
int obtener_transicion(AFD *afd, int estado_actual, char simbolo)
{
    Transicion *t = afd->estados[estado_actual].transiciones;
    while (t)
    {
        if (t->simbolo == simbolo)
        {
            return t->destino;
        }
        t = t->siguiente;
    }
    return -1; // No hay transición
}

// Función para validar una cadena
int validar_cadena(AFD *afd, const char *cadena)
{
    int estado_actual = afd->estado_inicial;
    int longitud = strlen(cadena);

    printf("\nProceso de validacion:\n");
    printf("Estado inicial: %s\n", afd->estados[estado_actual].nombre);

    for (int i = 0; i < longitud; i++)
    {
        char simbolo = cadena[i];

        // Verificar si el símbolo pertenece al alfabeto
        int simbolo_valido = 0;
        for (int j = 0; j < afd->num_simbolos; j++)
        {
            if (afd->alfabeto[j] == simbolo)
            {
                simbolo_valido = 1;
                break;
            }
        }

        if (!simbolo_valido)
        {
            printf("Error: El simbolo '%c' no pertenece al alfabeto\n", simbolo);
            return 0;
        }

        int siguiente_estado = obtener_transicion(afd, estado_actual, simbolo);
        if (siguiente_estado == -1)
        {
            printf("No hay transicion desde %s con simbolo '%c'\n",
                   afd->estados[estado_actual].nombre, simbolo);
            return 0;
        }

        printf("d(%s, %c) = %s\n",
               afd->estados[estado_actual].nombre,
               simbolo,
               afd->estados[siguiente_estado].nombre);

        estado_actual = siguiente_estado;
    }

    printf("Estado final: %s\n", afd->estados[estado_actual].nombre);

    // Verificar si el estado final es un estado de aceptación
    if (afd->estados[estado_actual].es_final)
    {
        printf("Cadena ACEPTADA (estado de aceptacion)\n");
        return 1;
    }
    else
    {
        printf("Cadena RECHAZADA (no es estado de aceptacion)\n");
        return 0;
    }
}

// Función para mostrar todas las transiciones de forma organizada
void mostrar_tabla_transiciones(AFD *afd)
{
    printf("\n=== TABLA DE TRANSICIONES ===\n");

    // Encabezado
    printf("Estado\t");
    for (int i = 0; i < afd->num_simbolos; i++)
    {
        printf("%c\t", afd->alfabeto[i]);
    }
    printf("\n");

    // Línea separadora
    for (int i = 0; i < (afd->num_simbolos + 1) * 8; i++)
    {
        printf("-");
    }
    printf("\n");

    // Filas de la tabla
    for (int i = 0; i < afd->num_estados; i++)
    {
        printf("%s", afd->estados[i].nombre);
        printf("\t");

        for (int j = 0; j < afd->num_simbolos; j++)
        {
            int destino = obtener_transicion(afd, i, afd->alfabeto[j]);
            if (destino != -1)
            {
                printf("%s\t", afd->estados[destino].nombre);
            }
            else
            {
                printf("-\t");
            }
        }
        printf("\n");
    }
    printf("\n+ = Estado inicial, * = Estado de aceptacion\n");
}

// Función para liberar memoria del autómata
void liberar_automata(AFD *afd)
{
    if (!afd)
        return;

    for (int i = 0; i < afd->num_estados; i++)
    {
        Transicion *actual = afd->estados[i].transiciones;
        while (actual)
        {
            Transicion *temp = actual;
            actual = actual->siguiente;
            free(temp);
        }
    }

    free(afd);
}

// Función para seleccionar archivo CSV
char *seleccionar_archivo_csv()
{
    static char nombre_archivo[256];
    printf("\n=== SELECCION DE ARCHIVO CSV ===\n");
    printf("Ingrese el nombre del archivo CSV (ejemplo: ejer1.csv): ");

    if (fgets(nombre_archivo, sizeof(nombre_archivo), stdin))
    {
        // Eliminar salto de línea si existe
        nombre_archivo[strcspn(nombre_archivo, "\n")] = 0;

        // Verificar si el archivo tiene extensión .csv
        if (strstr(nombre_archivo, ".csv") == NULL)
        {
            strcat(nombre_archivo, ".csv");
        }

        return nombre_archivo;
    }

    return NULL;
}

// Función para mostrar el menú
void mostrar_menu()
{
    printf("\n=== MENU DE AUTOMATAS FINITOS DETERMINISTAS ===\n");
    printf("1. Mostrar automata completo\n");
    printf("2. Validar cadena\n");
    printf("3. Mostrar tabla de transiciones\n");
    printf("4. Cargar nuevo archivo CSV\n");
    printf("5. Salir\n");
    printf("Seleccione una opcion: ");
}

// Función principal
int main()
{
    printf("=== PROGRAMA DE AUTOMATAS FINITOS DETERMINISTAS ===\n");

    // Seleccionar archivo CSV
    char *nombre_archivo = seleccionar_archivo_csv();
    if (!nombre_archivo)
    {
        printf("Error al obtener el nombre del archivo.\n");
        return 1;
    }

    printf("Cargando automata desde archivo CSV: %s\n", nombre_archivo);

    // Cargar el automata desde el archivo CSV
    AFD *automata = leer_afd_csv(nombre_archivo);
    if (!automata)
    {
        printf("Error al cargar el automata. Verifique que el archivo '%s' existe.\n", nombre_archivo);
        return 1;
    }

    printf("Automata cargado exitosamente!\n");
    printf("Estados: %d, Simbolos del alfabeto: %d\n",
           automata->num_estados, automata->num_simbolos);

    int opcion;
    char cadena[256];

    do
    {
        mostrar_menu();

        if (scanf("%d", &opcion) != 1)
        {
            printf("Error: Ingrese un numero valido.\n");
            // Limpiar buffer
            while (getchar() != '\n')
                ;
            continue;
        }

        // Limpiar buffer
        while (getchar() != '\n')
            ;

        switch (opcion)
        {
        case 1:
            mostrar_automata(automata);
            break;

        case 2:
            printf("\nIngrese la cadena a validar: ");
            if (fgets(cadena, sizeof(cadena), stdin))
            {
                // Eliminar salto de línea si existe
                cadena[strcspn(cadena, "\n")] = 0;

                if (strlen(cadena) == 0)
                {
                    printf("Cadena vacia - procesando cadena epsilon\n");
                    printf("Estado inicial: %s\n", automata->estados[automata->estado_inicial].nombre);
                    printf("Sin transiciones para cadena vacia\n");
                    printf("Estado final: %s\n", automata->estados[automata->estado_inicial].nombre);

                    if (automata->estados[automata->estado_inicial].es_final)
                    {
                        printf("Cadena ACEPTADA (estado inicial es de aceptacion)\n");
                    }
                    else
                    {
                        printf("Cadena RECHAZADA (estado inicial no es de aceptacion)\n");
                    }
                }
                else
                {
                    validar_cadena(automata, cadena);
                }
            }
            break;

        case 3:
            mostrar_tabla_transiciones(automata);
            break;

        case 4:
            // Cargar nuevo archivo CSV
            liberar_automata(automata);

            char *nuevo_archivo = seleccionar_archivo_csv();
            if (nuevo_archivo)
            {
                printf("Cargando nuevo automata desde archivo: %s\n", nuevo_archivo);
                automata = leer_afd_csv(nuevo_archivo);
                if (automata)
                {
                    printf("Nuevo automata cargado exitosamente!\n");
                    printf("Estados: %d, Simbolos del alfabeto: %d\n",
                           automata->num_estados, automata->num_simbolos);
                }
                else
                {
                    printf("Error al cargar el nuevo archivo. Saliendo del programa.\n");
                    return 1;
                }
            }
            break;

        case 5:
            printf("Saliendo del programa...\n");
            break;

        default:
            printf("Opcion invalida. Por favor seleccione una opcion del 1 al 5.\n");
            break;
        }

        if (opcion != 5)
        {
            printf("\nPresione Enter para continuar...");
            getchar();
        }

    } while (opcion != 5);

    // Liberar memoria
    liberar_automata(automata);

    printf("Gracias por usar el programa\n");
    return 0;
}
