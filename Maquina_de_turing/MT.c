#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef _WIN32
#include <windows.h>
#define CLEAR_SCREEN "cls"
#define SLEEP(ms) Sleep(ms)
#else
#include <unistd.h>
#define CLEAR_SCREEN "clear"
#define SLEEP(ms) usleep((ms) * 1000)
#endif

#define MAX_ESTADOS 20
#define MAX_SIMBOLOS 20
#define MAX_NOMBRE 10
#define MAX_LINEA 512
#define MAX_CINTA 1000
#define BLANK 'B'

// Estructura para una transición de MT
typedef struct TransicionMT
{
    char simbolo_lee;     // Símbolo que lee
    int estado_destino;   // Estado al que va
    char simbolo_escribe; // Símbolo que escribe
    char direccion;       // 'D' derecha, 'I' izquierda, 'S' stay
    struct TransicionMT *siguiente;
} TransicionMT;

// Estructura para representar un estado
typedef struct EstadoMT
{
    char nombre[MAX_NOMBRE];
    int id;
    int es_inicial;
    int es_final;
    TransicionMT *transiciones;
} EstadoMT;

// Estructura para la cinta
typedef struct Cinta
{
    char simbolos[MAX_CINTA];
    int cabezal; // Posición del cabezal
    int inicio;  // Inicio de la parte no-blanco
    int fin;     // Fin de la parte no-blanco
} Cinta;

// Estructura para la Máquina de Turing
typedef struct MT
{
    EstadoMT estados[MAX_ESTADOS];
    char alfabeto[MAX_SIMBOLOS];
    int num_estados;
    int num_simbolos;
    int estado_inicial;
    int es_reconocimiento; // 1 si tiene estados finales, 0 si es operacional
} MT;

// ============ FUNCIONES DE CINTA ============

void inicializar_cinta(Cinta *cinta, const char *entrada)
{
    // Inicializar toda la cinta con blancos
    for (int i = 0; i < MAX_CINTA; i++)
    {
        cinta->simbolos[i] = BLANK;
    }

    // Copiar entrada en el centro de la cinta
    int len = strlen(entrada);
    int pos_inicio = MAX_CINTA / 2;

    for (int i = 0; i < len; i++)
    {
        cinta->simbolos[pos_inicio + i] = entrada[i];
    }

    cinta->cabezal = pos_inicio;
    cinta->inicio = pos_inicio;
    cinta->fin = pos_inicio + len - 1;
}

void mostrar_cinta(Cinta *cinta, const char *estado_nombre, int paso)
{
    system(CLEAR_SCREEN);

    printf("\n=== MAQUINA DE TURING - Paso %d ===\n", paso);
    printf("Estado actual: %s\n\n", estado_nombre);

    // Mostrar un rango fijo de la cinta (la cinta se queda estática)
    int inicio_mostrar = cinta->inicio - 5;
    int fin_mostrar = cinta->fin + 15;

    if (inicio_mostrar < 0)
        inicio_mostrar = 0;
    if (fin_mostrar >= MAX_CINTA)
        fin_mostrar = MAX_CINTA - 1;

    // Asegurar un rango mínimo visible
    if (fin_mostrar - inicio_mostrar < 30)
    {
        fin_mostrar = inicio_mostrar + 30;
        if (fin_mostrar >= MAX_CINTA)
            fin_mostrar = MAX_CINTA - 1;
    }

    // Mostrar cinta (estática)
    printf("Cinta: [");
    for (int i = inicio_mostrar; i <= fin_mostrar; i++)
    {
        printf(" %c ", cinta->simbolos[i]);
    }
    printf("]\n");

    // Indicador del cabezal - SE MUEVE de posición
    printf("        ");
    for (int i = inicio_mostrar; i <= fin_mostrar; i++)
    {
        if (i == cinta->cabezal)
        {
            printf(" ^ ");
        }
        else
        {
            printf("   ");
        }
    }
    printf("\n");
}

void leer_cinta(Cinta *cinta, char *simbolo)
{
    *simbolo = cinta->simbolos[cinta->cabezal];
}

void escribir_cinta(Cinta *cinta, char simbolo)
{
    cinta->simbolos[cinta->cabezal] = simbolo;

    // Actualizar límites de contenido no-blanco
    if (simbolo != BLANK)
    {
        if (cinta->cabezal < cinta->inicio)
            cinta->inicio = cinta->cabezal;
        if (cinta->cabezal > cinta->fin)
            cinta->fin = cinta->cabezal;
    }
}

void mover_cabezal(Cinta *cinta, char direccion)
{
    if (direccion == 'D' || direccion == 'd')
    {
        if (cinta->cabezal < MAX_CINTA - 1)
            cinta->cabezal++;
    }
    else if (direccion == 'I' || direccion == 'i')
    {
        if (cinta->cabezal > 0)
            cinta->cabezal--;
    }
    // 'S' o 's' = stay (no se mueve)
}

void obtener_resultado_cinta(Cinta *cinta, char *resultado)
{
    int idx = 0;
    for (int i = cinta->inicio; i <= cinta->fin; i++)
    {
        if (cinta->simbolos[i] != BLANK)
        {
            resultado[idx++] = cinta->simbolos[i];
        }
    }
    resultado[idx] = '\0';
}

// ============ FUNCIONES DE MT ============

void agregar_transicion_mt(EstadoMT *estado, char simbolo_lee, int estado_destino,
                           char simbolo_escribe, char direccion)
{
    TransicionMT *nueva = (TransicionMT *)malloc(sizeof(TransicionMT));
    nueva->simbolo_lee = simbolo_lee;
    nueva->estado_destino = estado_destino;
    nueva->simbolo_escribe = simbolo_escribe;
    nueva->direccion = direccion;
    nueva->siguiente = estado->transiciones;
    estado->transiciones = nueva;
}

int encontrar_estado_mt(MT *mt, const char *nombre)
{
    for (int i = 0; i < mt->num_estados; i++)
    {
        if (strcmp(mt->estados[i].nombre, nombre) == 0)
        {
            return i;
        }
    }
    return -1;
}

// Parsear transición del formato "q1,0,D" o "q3,,,D" (coma como símbolo)
int parsear_transicion_mt(const char *str, char *estado_dest, char *simbolo_escribe, char *direccion)
{
    if (!str || strlen(str) == 0)
        return 0;

    // Buscar las comas manualmente para manejar coma como símbolo
    int len = strlen(str);
    int primera_coma = -1;
    int segunda_coma = -1;

    // Buscar primera coma
    for (int i = 0; i < len; i++)
    {
        if (str[i] == ',')
        {
            primera_coma = i;
            break;
        }
    }

    if (primera_coma == -1)
        return 0;

    // Buscar segunda coma (desde el final, ya que puede haber comas en medio)
    for (int i = len - 1; i > primera_coma; i--)
    {
        if (str[i] == ',')
        {
            segunda_coma = i;
            break;
        }
    }

    if (segunda_coma == -1)
        return 0;

    // Extraer estado destino (antes de primera coma)
    int idx = 0;
    for (int i = 0; i < primera_coma; i++)
    {
        estado_dest[idx++] = str[i];
    }
    estado_dest[idx] = '\0';

    // Extraer símbolo a escribir (entre primera y segunda coma)
    // Si hay más de una coma entre ellas, el símbolo ES una coma
    int medio_len = segunda_coma - primera_coma - 1;
    if (medio_len == 0)
    {
        *simbolo_escribe = ','; // El símbolo es una coma
    }
    else if (medio_len == 1)
    {
        *simbolo_escribe = str[primera_coma + 1];
    }
    else
    {
        // Múltiples caracteres en medio - tomar el que no sea coma
        int found = 0;
        for (int i = primera_coma + 1; i < segunda_coma; i++)
        {
            if (str[i] != ',')
            {
                *simbolo_escribe = str[i];
                found = 1;
                break;
            }
        }
        if (!found)
            *simbolo_escribe = ','; // Todo son comas, el símbolo es coma
    }

    // Extraer dirección (después de segunda coma)
    *direccion = str[segunda_coma + 1];

    return 1;
}

MT *leer_mt_csv(const char *nombre_archivo)
{
    FILE *archivo = fopen(nombre_archivo, "r");
    if (!archivo)
    {
        printf("Error: No se pudo abrir el archivo %s\n", nombre_archivo);
        return NULL;
    }

    MT *mt = (MT *)malloc(sizeof(MT));
    mt->num_estados = 0;
    mt->num_simbolos = 0;
    mt->estado_inicial = 0;
    mt->es_reconocimiento = 0;

    // Inicializar todos los estados
    for (int i = 0; i < MAX_ESTADOS; i++)
    {
        mt->estados[i].transiciones = NULL;
        mt->estados[i].es_inicial = 0;
        mt->estados[i].es_final = 0;
    }

    char linea[MAX_LINEA];
    char simbolos_headers[MAX_SIMBOLOS][MAX_NOMBRE];

    // Leer primera línea (alfabeto)
    if (fgets(linea, sizeof(linea), archivo))
    {
        int i = 0;
        int dentro_comillas = 0;
        char buffer[MAX_NOMBRE] = "";
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
                    strcpy(simbolos_headers[mt->num_simbolos], buffer);
                    mt->alfabeto[mt->num_simbolos] = buffer[0];
                    mt->num_simbolos++;
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

        // Último símbolo
        if (buffer_idx > 0 && !primera_columna)
        {
            buffer[buffer_idx] = '\0';
            strcpy(simbolos_headers[mt->num_simbolos], buffer);
            mt->alfabeto[mt->num_simbolos] = buffer[0];
            mt->num_simbolos++;
        }
    }

    // Leer estados y transiciones
    char estados_temp[MAX_ESTADOS][MAX_NOMBRE];
    char trans_destinos[MAX_ESTADOS][MAX_SIMBOLOS][MAX_LINEA];

    while (fgets(linea, sizeof(linea), archivo) && mt->num_estados < MAX_ESTADOS)
    {
        // Parser manual para respetar celdas vacías
        int i = 0;
        int col = 0;
        char buffer[MAX_LINEA] = "";
        int buffer_idx = 0;
        int dentro_comillas = 0;

        // Leer nombre del estado (primera columna)
        while (linea[i] != '\0' && linea[i] != ',' && linea[i] != '\n' && linea[i] != '\r')
        {
            buffer[buffer_idx++] = linea[i++];
        }
        buffer[buffer_idx] = '\0';

        if (strlen(buffer) == 0)
            continue;

        strcpy(estados_temp[mt->num_estados], buffer);
        strcpy(mt->estados[mt->num_estados].nombre, buffer);
        mt->estados[mt->num_estados].id = mt->num_estados;

        // Verificar si es estado inicial (+)
        if (strchr(buffer, '+') != NULL)
        {
            mt->estados[mt->num_estados].es_inicial = 1;
            mt->estado_inicial = mt->num_estados;
        }

        // Verificar si es estado de aceptación (*)
        if (strchr(buffer, '*') != NULL)
        {
            mt->estados[mt->num_estados].es_final = 1;
            mt->es_reconocimiento = 1;
        }

        // Saltar la coma después del nombre
        if (linea[i] == ',')
            i++;

        // Leer destinos de transiciones
        col = 0;
        buffer_idx = 0;
        buffer[0] = '\0';
        dentro_comillas = 0;

        while (linea[i] != '\0' && linea[i] != '\n' && linea[i] != '\r' && col < mt->num_simbolos)
        {
            if (linea[i] == '"')
            {
                dentro_comillas = !dentro_comillas;
                i++;
                continue;
            }

            if (linea[i] == ',' && !dentro_comillas)
            {
                // Fin de celda
                buffer[buffer_idx] = '\0';
                strcpy(trans_destinos[mt->num_estados][col], buffer);
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
        if (col < mt->num_simbolos)
        {
            buffer[buffer_idx] = '\0';
            strcpy(trans_destinos[mt->num_estados][col], buffer);
            col++;
        }

        // Rellenar celdas restantes
        while (col < mt->num_simbolos)
        {
            strcpy(trans_destinos[mt->num_estados][col], "");
            col++;
        }

        mt->num_estados++;
    }

    // Crear las transiciones
    for (int i = 0; i < mt->num_estados; i++)
    {
        for (int j = 0; j < mt->num_simbolos; j++)
        {
            if (strlen(trans_destinos[i][j]) > 0)
            {
                char estado_dest[MAX_NOMBRE];
                char simbolo_escribe;
                char direccion;

                if (parsear_transicion_mt(trans_destinos[i][j], estado_dest,
                                          &simbolo_escribe, &direccion))
                {
                    // Buscar estado destino
                    int destino = -1;
                    for (int k = 0; k < mt->num_estados; k++)
                    {
                        char nombre_sin_marcas[MAX_NOMBRE];
                        strcpy(nombre_sin_marcas, mt->estados[k].nombre);

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

                        if (strcmp(nombre_sin_marcas, estado_dest) == 0)
                        {
                            destino = k;
                            break;
                        }
                    }

                    if (destino != -1)
                    {
                        agregar_transicion_mt(&mt->estados[i], mt->alfabeto[j],
                                              destino, simbolo_escribe, direccion);
                    }
                }
            }
        }
    }

    fclose(archivo);
    return mt;
}

TransicionMT *buscar_transicion_mt(MT *mt, int estado_actual, char simbolo)
{
    EstadoMT *estado = &mt->estados[estado_actual];
    TransicionMT *t = estado->transiciones;

    while (t)
    {
        if (t->simbolo_lee == simbolo)
        {
            return t;
        }
        t = t->siguiente;
    }
    return NULL;
}

void mostrar_mt(MT *mt)
{
    printf("\n=== MAQUINA DE TURING ===\n");
    printf("Tipo: %s\n", mt->es_reconocimiento ? "RECONOCIMIENTO" : "OPERACIONAL");
    printf("Alfabeto: {");
    for (int i = 0; i < mt->num_simbolos; i++)
    {
        printf("%c", mt->alfabeto[i]);
        if (i < mt->num_simbolos - 1)
            printf(", ");
    }
    printf("}\n\n");

    printf("Estados y Transiciones:\n\n");

    for (int i = 0; i < mt->num_estados; i++)
    {
        printf("Estado %s", mt->estados[i].nombre);
        if (mt->estados[i].es_inicial)
            printf(" (inicial)");
        if (mt->estados[i].es_final)
            printf(" (aceptacion)");
        printf(":\n");

        TransicionMT *t = mt->estados[i].transiciones;
        while (t)
        {
            printf("  delta(%s, %c) = (%s, %c, %c)\n",
                   mt->estados[i].nombre,
                   t->simbolo_lee,
                   mt->estados[t->estado_destino].nombre,
                   t->simbolo_escribe,
                   t->direccion);
            t = t->siguiente;
        }
        printf("\n");
    }
}

// Estructura para guardar el historial de pasos
typedef struct HistorialPaso
{
    int paso;
    char estado[MAX_NOMBRE];
    char simbolo_leido;
    char simbolo_escrito;
    char direccion;
    int posicion_cabezal;
} HistorialPaso;

int ejecutar_mt(MT *mt, const char *entrada, char *resultado, int animacion, HistorialPaso **historial_out, int *num_pasos_out)
{
    Cinta cinta;
    inicializar_cinta(&cinta, entrada);

    int estado_actual = mt->estado_inicial;
    int paso = 0;
    int max_pasos = 10000; // Prevenir loops infinitos

    // Historial de pasos para ejecución rápida
    HistorialPaso *historial = NULL;
    int num_pasos_guardados = 0;

    if (!animacion)
    {
        historial = (HistorialPaso *)malloc(max_pasos * sizeof(HistorialPaso));
    }

    if (animacion)
    {
        mostrar_cinta(&cinta, mt->estados[estado_actual].nombre, paso);
        printf("\nPresione Enter para comenzar...");
        getchar();
    }

    while (paso < max_pasos)
    {
        paso++;

        // Leer símbolo actual
        char simbolo_actual;
        leer_cinta(&cinta, &simbolo_actual);

        // Buscar transición
        TransicionMT *trans = buscar_transicion_mt(mt, estado_actual, simbolo_actual);

        if (!trans)
        {
            // No hay transición - MT se detiene
            if (animacion)
            {
                mostrar_cinta(&cinta, mt->estados[estado_actual].nombre, paso);
                printf("\nNo hay transicion para (%s, %c)\n",
                       mt->estados[estado_actual].nombre, simbolo_actual);
                printf("Maquina DETENIDA\n");
            }
            else
            {
                // Guardar último paso sin transición
                HistorialPaso p;
                p.paso = paso;
                strcpy(p.estado, mt->estados[estado_actual].nombre);
                p.simbolo_leido = simbolo_actual;
                p.simbolo_escrito = simbolo_actual;
                p.direccion = 'S';
                p.posicion_cabezal = cinta.cabezal;
                historial[num_pasos_guardados++] = p;
            }

            obtener_resultado_cinta(&cinta, resultado);

            if (!animacion && historial_out && num_pasos_out)
            {
                *historial_out = historial;
                *num_pasos_out = num_pasos_guardados;
            }
            else if (!animacion)
            {
                free(historial);
            }

            if (mt->es_reconocimiento)
            {
                return mt->estados[estado_actual].es_final;
            }
            else
            {
                return 1; // Operacional: siempre retorna resultado
            }
        }

        // Guardar paso en historial (modo rápido)
        if (!animacion)
        {
            HistorialPaso p;
            p.paso = paso;
            strcpy(p.estado, mt->estados[estado_actual].nombre);
            p.simbolo_leido = simbolo_actual;
            p.simbolo_escrito = trans->simbolo_escribe;
            p.direccion = trans->direccion;
            p.posicion_cabezal = cinta.cabezal;
            historial[num_pasos_guardados++] = p;
        }

        // Aplicar transición
        escribir_cinta(&cinta, trans->simbolo_escribe);
        mover_cabezal(&cinta, trans->direccion);
        estado_actual = trans->estado_destino;

        // Mostrar paso (modo animación)
        if (animacion)
        {
            mostrar_cinta(&cinta, mt->estados[estado_actual].nombre, paso);
            printf("\nTransicion: delta(..., %c) -> (%s, %c, %c)\n",
                   simbolo_actual,
                   mt->estados[estado_actual].nombre,
                   trans->simbolo_escribe,
                   trans->direccion);

            SLEEP(500); // Pausa de 500ms
        }

        // En MT de reconocimiento, verificar si llegó a estado final
        if (mt->es_reconocimiento && mt->estados[estado_actual].es_final)
        {
            if (animacion)
            {
                printf("\nEstado de ACEPTACION alcanzado\n");
            }
            obtener_resultado_cinta(&cinta, resultado);

            if (!animacion && historial_out && num_pasos_out)
            {
                *historial_out = historial;
                *num_pasos_out = num_pasos_guardados;
            }
            else if (!animacion)
            {
                free(historial);
            }
            return 1;
        }
    }

    if (animacion)
    {
        printf("\nLimite de pasos alcanzado (%d). Posible loop infinito.\n", max_pasos);
    }

    obtener_resultado_cinta(&cinta, resultado);

    if (!animacion && historial_out && num_pasos_out)
    {
        *historial_out = historial;
        *num_pasos_out = num_pasos_guardados;
    }
    else if (!animacion)
    {
        free(historial);
    }
    return 0;
}

void mostrar_historial_pasos(HistorialPaso *historial, int num_pasos)
{
    printf("\n=== HISTORIAL DE EJECUCION ===\n");
    printf("Total de pasos: %d\n\n", num_pasos);

    for (int i = 0; i < num_pasos; i++)
    {
        printf("Paso %d:\n", historial[i].paso);
        printf("  Estado: %s\n", historial[i].estado);
        printf("  Simbolo leido: %c\n", historial[i].simbolo_leido);
        printf("  Simbolo escrito: %c\n", historial[i].simbolo_escrito);
        printf("  Direccion: %c\n", historial[i].direccion);
        printf("  Posicion cabezal: %d\n", historial[i].posicion_cabezal);
        printf("\n");
    }
}

void liberar_mt(MT *mt)
{
    if (!mt)
        return;

    for (int i = 0; i < mt->num_estados; i++)
    {
        TransicionMT *actual = mt->estados[i].transiciones;
        while (actual)
        {
            TransicionMT *temp = actual;
            actual = actual->siguiente;
            free(temp);
        }
    }
    free(mt);
}

void mostrar_menu()
{
    printf("\n=== MENU MAQUINA DE TURING ===\n");
    printf("1. Mostrar maquina de Turing\n");
    printf("2. Ejecutar con animacion\n");
    printf("3. Ejecutar sin animacion (rapido)\n");
    printf("4. Cargar nuevo archivo CSV\n");
    printf("5. Salir\n");
    printf("Seleccione una opcion: ");
}

int main()
{
    printf("=== PROGRAMA DE MAQUINA DE TURING ===\n");

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

    MT *mt = leer_mt_csv(nombre_archivo);
    if (!mt)
    {
        printf("Error al cargar la maquina de Turing.\n");
        return 1;
    }

    printf("Maquina de Turing cargada exitosamente!\n");
    printf("Estados: %d, Tipo: %s\n",
           mt->num_estados,
           mt->es_reconocimiento ? "RECONOCIMIENTO" : "OPERACIONAL");

    int opcion;
    char entrada[256];
    char resultado[MAX_CINTA];

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
            mostrar_mt(mt);
            break;

        case 2:
        case 3:
            printf("\nIngrese la cadena de entrada: ");
            if (fgets(entrada, sizeof(entrada), stdin))
            {
                entrada[strcspn(entrada, "\n")] = 0;

                HistorialPaso *historial = NULL;
                int num_pasos = 0;

                int acepta = ejecutar_mt(mt, entrada, resultado, opcion == 2, &historial, &num_pasos);

                // Mostrar historial si es ejecución rápida
                if (opcion == 3 && historial)
                {
                    mostrar_historial_pasos(historial, num_pasos);
                    free(historial);
                }

                printf("\n=== RESULTADO FINAL ===\n");
                if (mt->es_reconocimiento)
                {
                    printf("Cadena %s\n", acepta ? "ACEPTADA" : "RECHAZADA");
                }
                else
                {
                    printf("Resultado: %s\n", resultado);
                }
            }
            break;

        case 4:
            liberar_mt(mt);
            printf("\nIngrese el nombre del nuevo archivo CSV: ");
            if (fgets(nombre_archivo, sizeof(nombre_archivo), stdin))
            {
                nombre_archivo[strcspn(nombre_archivo, "\n")] = 0;
                if (strstr(nombre_archivo, ".csv") == NULL)
                {
                    strcat(nombre_archivo, ".csv");
                }
                mt = leer_mt_csv(nombre_archivo);
                if (mt)
                {
                    printf("Nueva maquina cargada!\n");
                }
                else
                {
                    printf("Error al cargar. Saliendo...\n");
                    return 1;
                }
            }
            break;

        case 5:
            printf("Saliendo...\n");
            break;

        default:
            printf("Opcion invalida.\n");
        }

        if (opcion != 5)
        {
            printf("\nPresione Enter para continuar...");
            getchar();
        }

    } while (opcion != 5);

    liberar_mt(mt);
    printf("Gracias por usar el programa\n");
    return 0;
}
