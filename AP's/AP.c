#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_ESTADOS 20
#define MAX_TRANSICIONES 50
#define MAX_NOMBRE 10
#define MAX_LINEA 256
#define MAX_PILA 1000

// Estructura para una transición de autómata de pila
// Formato: "simbolo,tope_pila/resultado_pila"
typedef struct TransicionPDA
{
    char simbolo_entrada;           // 'a', 'b', 'e' (epsilon)
    char simbolo_tope[MAX_NOMBRE];  // símbolo en el tope de la pila (ej: "z0", "a")
    char simbolos_push[MAX_NOMBRE]; // símbolos a meter en la pila (ej: "aaz0", "e" para pop)
    int estado_destino;
    struct TransicionPDA *siguiente;
} TransicionPDA;

// Estructura para representar un estado
typedef struct EstadoPDA
{
    char nombre[MAX_NOMBRE];
    int id;
    int es_inicial;
    int es_final;
    TransicionPDA *transiciones;
} EstadoPDA;

// Estructura para la pila
typedef struct Pila
{
    char simbolos[MAX_PILA];
    int tope; // índice del tope (-1 si vacía)
} Pila;

// Estructura para el autómata de pila
typedef struct PDA
{
    EstadoPDA estados[MAX_ESTADOS];
    int num_estados;
    int estado_inicial;
} PDA;

// ============ FUNCIONES DE PILA ============

void inicializar_pila(Pila *p)
{
    p->tope = -1;
}

int pila_vacia(Pila *p)
{
    return p->tope == -1;
}

void push_pila(Pila *p, char simbolo)
{
    if (p->tope < MAX_PILA - 1)
    {
        p->tope++;
        p->simbolos[p->tope] = simbolo;
    }
}

char pop_pila(Pila *p)
{
    if (!pila_vacia(p))
    {
        char simbolo = p->simbolos[p->tope];
        p->tope--;
        return simbolo;
    }
    return '\0';
}

char ver_tope(Pila *p)
{
    if (!pila_vacia(p))
    {
        return p->simbolos[p->tope];
    }
    return '\0';
}

// Verificar si el tope de la pila coincide con un símbolo (puede ser "z0" para vacío)
int tope_coincide(Pila *p, const char *simbolo_esperado)
{
    if (strcmp(simbolo_esperado, "z0") == 0)
    {
        return pila_vacia(p);
    }
    if (pila_vacia(p))
    {
        return 0;
    }
    return ver_tope(p) == simbolo_esperado[0];
}

void mostrar_pila(Pila *p)
{
    printf("[");
    for (int i = p->tope; i >= 0; i--)
    {
        printf("%c", p->simbolos[i]);
    }
    if (pila_vacia(p))
    {
        printf("z0");
    }
    printf("]");
}

// ============ FUNCIONES DEL PDA ============

void agregar_transicion_pda(EstadoPDA *estado, char simbolo_entrada,
                            const char *tope_pila, const char *push_simbolos,
                            int estado_destino)
{
    TransicionPDA *nueva = (TransicionPDA *)malloc(sizeof(TransicionPDA));
    nueva->simbolo_entrada = simbolo_entrada;
    strcpy(nueva->simbolo_tope, tope_pila);
    strcpy(nueva->simbolos_push, push_simbolos);
    nueva->estado_destino = estado_destino;
    nueva->siguiente = estado->transiciones;
    estado->transiciones = nueva;
}

int encontrar_estado_pda(PDA *pda, const char *nombre)
{
    for (int i = 0; i < pda->num_estados; i++)
    {
        if (strcmp(pda->estados[i].nombre, nombre) == 0)
        {
            return i;
        }
    }
    return -1;
}

// Parsear transición del formato "a,z0/aaz0"
// Retorna: 1 si es válida, 0 si no
int parsear_transicion(const char *str, char *simbolo_entrada,
                       char *tope_pila, char *push_simbolos)
{
    if (!str || strlen(str) == 0)
        return 0;

    // Buscar la coma
    const char *coma = strchr(str, ',');
    if (!coma)
        return 0;

    // Símbolo de entrada (antes de la coma)
    *simbolo_entrada = str[0];

    // Buscar la barra diagonal
    const char *barra = strchr(coma, '/');
    if (!barra)
        return 0;

    // Extraer tope_pila (entre coma y barra)
    int len_tope = barra - (coma + 1);
    strncpy(tope_pila, coma + 1, len_tope);
    tope_pila[len_tope] = '\0';

    // Extraer push_simbolos (después de la barra)
    strcpy(push_simbolos, barra + 1);

    return 1;
}

PDA *leer_pda_csv(const char *nombre_archivo)
{
    FILE *archivo = fopen(nombre_archivo, "r");
    if (!archivo)
    {
        printf("Error: No se pudo abrir el archivo %s\n", nombre_archivo);
        return NULL;
    }

    PDA *pda = (PDA *)malloc(sizeof(PDA));
    pda->num_estados = 0;
    pda->estado_inicial = 0;

    // Inicializar todos los estados
    for (int i = 0; i < MAX_ESTADOS; i++)
    {
        pda->estados[i].transiciones = NULL;
        pda->estados[i].es_inicial = 0;
        pda->estados[i].es_final = 0;
    }

    char linea[MAX_LINEA];
    char transiciones_headers[MAX_TRANSICIONES][MAX_LINEA];
    int num_transiciones = 0;

    // Leer primera línea (encabezados de transiciones)
    if (fgets(linea, sizeof(linea), archivo))
    {
        // Procesar manualmente para manejar comillas correctamente
        int i = 0;
        int dentro_comillas = 0;
        char buffer[MAX_LINEA] = "";
        int buffer_idx = 0;
        int primera_columna = 1;

        while (linea[i] != '\0' && linea[i] != '\n' && linea[i] != '\r')
        {
            if (linea[i] == '"')
            {
                dentro_comillas = !dentro_comillas;
                i++;
                continue;
            }

            if (linea[i] == ',' && !dentro_comillas)
            {
                if (primera_columna)
                {
                    primera_columna = 0;
                }
                else if (buffer_idx > 0)
                {
                    buffer[buffer_idx] = '\0';
                    strcpy(transiciones_headers[num_transiciones], buffer);
                    num_transiciones++;
                    buffer_idx = 0;
                }
                i++;
                continue;
            }

            if (!primera_columna)
            {
                buffer[buffer_idx++] = linea[i];
            }
            i++;
        }

        // Última transición si hay algo en el buffer
        if (buffer_idx > 0 && !primera_columna)
        {
            buffer[buffer_idx] = '\0';
            strcpy(transiciones_headers[num_transiciones], buffer);
            num_transiciones++;
        }
    }

    // Leer estados y construir transiciones
    char estados_temp[MAX_ESTADOS][MAX_NOMBRE];
    char trans_destinos[MAX_ESTADOS][MAX_TRANSICIONES][MAX_NOMBRE];

    while (fgets(linea, sizeof(linea), archivo) && pda->num_estados < MAX_ESTADOS)
    {
        // Parser manual para respetar celdas vacías
        int i = 0;
        int col = 0;
        char buffer[MAX_NOMBRE] = "";
        int buffer_idx = 0;

        // Leer nombre del estado (primera columna)
        while (linea[i] != '\0' && linea[i] != ',' && linea[i] != '\n' && linea[i] != '\r')
        {
            buffer[buffer_idx++] = linea[i++];
        }
        buffer[buffer_idx] = '\0';

        if (strlen(buffer) == 0)
            continue;

        strcpy(estados_temp[pda->num_estados], buffer);
        strcpy(pda->estados[pda->num_estados].nombre, buffer);
        pda->estados[pda->num_estados].id = pda->num_estados;

        // Verificar si es estado inicial (+)
        if (strchr(buffer, '+') != NULL)
        {
            pda->estados[pda->num_estados].es_inicial = 1;
            pda->estado_inicial = pda->num_estados;
        }

        // Verificar si es estado de aceptación (*)
        if (strchr(buffer, '*') != NULL)
        {
            pda->estados[pda->num_estados].es_final = 1;
        }

        // Saltar la coma después del nombre
        if (linea[i] == ',')
            i++;

        // Leer destinos de transiciones
        col = 0;
        buffer_idx = 0;
        buffer[0] = '\0';

        while (linea[i] != '\0' && linea[i] != '\n' && linea[i] != '\r' && col < num_transiciones)
        {
            if (linea[i] == ',')
            {
                // Fin de celda
                buffer[buffer_idx] = '\0';
                strcpy(trans_destinos[pda->num_estados][col], buffer);
                col++;
                buffer_idx = 0;
                buffer[0] = '\0';
                i++;
            }
            else
            {
                buffer[buffer_idx++] = linea[i++];
            }
        }

        // Última celda
        if (col < num_transiciones)
        {
            buffer[buffer_idx] = '\0';
            strcpy(trans_destinos[pda->num_estados][col], buffer);
            col++;
        }

        // Rellenar celdas restantes con vacío
        while (col < num_transiciones)
        {
            strcpy(trans_destinos[pda->num_estados][col], "");
            col++;
        }

        pda->num_estados++;
    }

    // Crear las transiciones
    for (int i = 0; i < pda->num_estados; i++)
    {
        for (int j = 0; j < num_transiciones; j++)
        {
            if (strlen(trans_destinos[i][j]) > 0)
            {
                // Buscar estado destino
                int destino = -1;
                for (int k = 0; k < pda->num_estados; k++)
                {
                    char nombre_sin_marcas[MAX_NOMBRE];
                    strcpy(nombre_sin_marcas, pda->estados[k].nombre);

                    // Remover + y *
                    int len = strlen(nombre_sin_marcas);
                    for (int idx = len - 1; idx >= 0; idx--)
                    {
                        if (nombre_sin_marcas[idx] == '+' || nombre_sin_marcas[idx] == '*')
                        {
                            nombre_sin_marcas[idx] = '\0';
                        }
                        else
                        {
                            break;
                        }
                    }

                    if (strcmp(nombre_sin_marcas, trans_destinos[i][j]) == 0)
                    {
                        destino = k;
                        break;
                    }
                }

                if (destino != -1)
                {
                    // Parsear la transición
                    char simbolo_entrada, tope_pila[MAX_NOMBRE], push_simbolos[MAX_NOMBRE];
                    if (parsear_transicion(transiciones_headers[j], &simbolo_entrada,
                                           tope_pila, push_simbolos))
                    {
                        agregar_transicion_pda(&pda->estados[i], simbolo_entrada,
                                               tope_pila, push_simbolos, destino);
                    }
                }
            }
        }
    }

    fclose(archivo);
    return pda;
}

void mostrar_pda(PDA *pda)
{
    printf("\n=== AUTOMATA DE PILA (PDA) ===\n");
    printf("Estados y Transiciones:\n\n");

    for (int i = 0; i < pda->num_estados; i++)
    {
        printf("Estado %s", pda->estados[i].nombre);
        if (pda->estados[i].es_inicial)
            printf(" (inicial)");
        if (pda->estados[i].es_final)
            printf(" (aceptacion)");
        printf(":\n");

        TransicionPDA *t = pda->estados[i].transiciones;
        while (t)
        {
            printf("  delta(%s, '%c', %s) = (%s, %s)\n",
                   pda->estados[i].nombre,
                   t->simbolo_entrada,
                   t->simbolo_tope,
                   pda->estados[t->estado_destino].nombre,
                   t->simbolos_push);
            t = t->siguiente;
        }
        printf("\n");
    }
}

TransicionPDA *buscar_transicion(PDA *pda, int estado_actual, char simbolo, Pila *pila)
{
    EstadoPDA *estado = &pda->estados[estado_actual];
    TransicionPDA *t = estado->transiciones;

    while (t)
    {
        if (t->simbolo_entrada == simbolo && tope_coincide(pila, t->simbolo_tope))
        {
            return t;
        }
        t = t->siguiente;
    }
    return NULL;
}

// Buscar transición epsilon
TransicionPDA *buscar_transicion_epsilon(PDA *pda, int estado_actual, Pila *pila)
{
    EstadoPDA *estado = &pda->estados[estado_actual];
    TransicionPDA *t = estado->transiciones;

    while (t)
    {
        if (t->simbolo_entrada == 'e' && tope_coincide(pila, t->simbolo_tope))
        {
            return t;
        }
        t = t->siguiente;
    }
    return NULL;
}

void aplicar_transicion_pila(Pila *pila, const char *tope_esperado, const char *push_simbolos)
{
    // Si no es vacío (z0), sacar el símbolo del tope
    if (strcmp(tope_esperado, "z0") != 0)
    {
        pop_pila(pila);
    }

    // Si push_simbolos no es 'e' (epsilon/pop), meter símbolos
    if (strcmp(push_simbolos, "e") != 0)
    {
        // Meter símbolos en orden inverso (el último caracter queda arriba)
        // IMPORTANTE: Solo filtrar "z0" cuando aparecen juntos como marcador de pila vacía
        int len = strlen(push_simbolos);

        // Verificar si termina con "z0" (marcador de pila vacía)
        if (len >= 2 && push_simbolos[len - 2] == 'z' && push_simbolos[len - 1] == '0')
        {
            len -= 2; // No meter los últimos dos caracteres (z0)
        }

        // Meter símbolos en orden inverso
        for (int i = len - 1; i >= 0; i--)
        {
            push_pila(pila, push_simbolos[i]);
        }
    }
}

int validar_cadena_pda(PDA *pda, const char *cadena)
{
    int estado_actual = pda->estado_inicial;
    Pila pila;
    inicializar_pila(&pila);

    int longitud = strlen(cadena);

    printf("\n=== PROCESO DE VALIDACION ===\n");
    printf("Cadena: \"%s\"\n", cadena);
    printf("Estado inicial: %s, Pila: ", pda->estados[estado_actual].nombre);
    mostrar_pila(&pila);
    printf("\n\n");

    for (int i = 0; i < longitud; i++)
    {
        char simbolo = cadena[i];

        printf("Paso %d: Leyendo '%c'\n", i + 1, simbolo);
        printf("  Estado actual: %s, Pila: ", pda->estados[estado_actual].nombre);
        mostrar_pila(&pila);
        printf("\n");

        // Buscar transición aplicable
        TransicionPDA *trans = buscar_transicion(pda, estado_actual, simbolo, &pila);

        if (!trans)
        {
            printf("  ERROR: No hay transicion valida\n");
            return 0;
        }

        printf("  Transicion: delta(%s, '%c', %s) = (%s, %s)\n",
               pda->estados[estado_actual].nombre,
               simbolo,
               trans->simbolo_tope,
               pda->estados[trans->estado_destino].nombre,
               trans->simbolos_push);

        // Aplicar transición
        aplicar_transicion_pila(&pila, trans->simbolo_tope, trans->simbolos_push);
        estado_actual = trans->estado_destino;

        printf("  Nueva pila: ");
        mostrar_pila(&pila);
        printf(", Nuevo estado: %s\n\n", pda->estados[estado_actual].nombre);
    }

    // Intentar transiciones epsilon hasta llegar a estado final
    printf("Intentando transiciones epsilon...\n");
    int intentos = 0;
    int hubo_transicion = 0;
    while (intentos < 10)
    {
        TransicionPDA *trans_eps = buscar_transicion_epsilon(pda, estado_actual, &pila);
        if (!trans_eps)
            break;

        hubo_transicion = 1;
        printf("  epsilon-transicion: delta(%s, 'e', %s) = (%s, %s)\n",
               pda->estados[estado_actual].nombre,
               trans_eps->simbolo_tope,
               pda->estados[trans_eps->estado_destino].nombre,
               trans_eps->simbolos_push);

        aplicar_transicion_pila(&pila, trans_eps->simbolo_tope, trans_eps->simbolos_push);
        estado_actual = trans_eps->estado_destino;

        printf("  Pila: ");
        mostrar_pila(&pila);
        printf(", Estado: %s\n", pda->estados[estado_actual].nombre);

        intentos++;
    }

    if (!hubo_transicion)
    {
        printf("  No hay transiciones epsilon disponibles\n");
    }

    printf("\n=== RESULTADO ===\n");
    printf("Estado final: %s\n", pda->estados[estado_actual].nombre);
    printf("Pila final: ");
    mostrar_pila(&pila);
    printf("\n");

    if (pda->estados[estado_actual].es_final)
    {
        printf("\nCadena ACEPTADA\n");
        return 1;
    }
    else
    {
        printf("\nCadena RECHAZADA (no alcanzo estado de aceptacion)\n");
        return 0;
    }
}

void liberar_pda(PDA *pda)
{
    if (!pda)
        return;

    for (int i = 0; i < pda->num_estados; i++)
    {
        TransicionPDA *actual = pda->estados[i].transiciones;
        while (actual)
        {
            TransicionPDA *temp = actual;
            actual = actual->siguiente;
            free(temp);
        }
    }
    free(pda);
}

void mostrar_menu()
{
    printf("\n=== MENU AUTOMATA DE PILA ===\n");
    printf("1. Mostrar automata\n");
    printf("2. Validar cadena\n");
    printf("3. Cargar nuevo archivo CSV\n");
    printf("4. Salir\n");
    printf("Seleccione una opcion: ");
}

int main()
{
    printf("=== PROGRAMA DE AUTOMATA DE PILA (PDA) ===\n");

    char nombre_archivo[256];
    printf("\nIngrese el nombre del archivo CSV: ");
    if (fgets(nombre_archivo, sizeof(nombre_archivo), stdin))
    {
        nombre_archivo[strcspn(nombre_archivo, "\n")] = 0;
        if (strstr(nombre_archivo, ".csv") == NULL)
        {
            strcat(nombre_archivo, ".csv");
        }
    }

    PDA *pda = leer_pda_csv(nombre_archivo);
    if (!pda)
    {
        printf("Error al cargar el automata.\n");
        return 1;
    }

    printf("Automata cargado exitosamente!\n");
    printf("Estados: %d\n", pda->num_estados);

    int opcion;
    char cadena[256];

    do
    {
        mostrar_menu();

        if (scanf("%d", &opcion) != 1)
        {
            printf("Error: Ingrese un numero valido.\n");
            while (getchar() != '\n')
                ;
            continue;
        }
        while (getchar() != '\n')
            ;

        switch (opcion)
        {
        case 1:
            mostrar_pda(pda);
            break;

        case 2:
            printf("\nIngrese la cadena a validar: ");
            if (fgets(cadena, sizeof(cadena), stdin))
            {
                cadena[strcspn(cadena, "\n")] = 0;
                validar_cadena_pda(pda, cadena);
            }
            break;

        case 3:
            liberar_pda(pda);
            printf("\nIngrese el nombre del nuevo archivo CSV: ");
            if (fgets(nombre_archivo, sizeof(nombre_archivo), stdin))
            {
                nombre_archivo[strcspn(nombre_archivo, "\n")] = 0;
                if (strstr(nombre_archivo, ".csv") == NULL)
                {
                    strcat(nombre_archivo, ".csv");
                }
                pda = leer_pda_csv(nombre_archivo);
                if (pda)
                {
                    printf("Nuevo automata cargado!\n");
                }
                else
                {
                    printf("Error al cargar. Saliendo...\n");
                    return 1;
                }
            }
            break;

        case 4:
            printf("Saliendo...\n");
            break;

        default:
            printf("Opcion invalida.\n");
        }

        if (opcion != 4)
        {
            printf("\nPresione Enter para continuar...");
            getchar();
        }

    } while (opcion != 4);

    liberar_pda(pda);
    printf("Gracias por usar el programa\n");
    return 0;
}
