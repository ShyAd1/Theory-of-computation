/* cadenas_markov.c
   Lee frases.txt, convierte letras a 'v' (vocal) o 'c' (consonante),
   calcula probabilidades de bigramas (vv, vc, cv, cc), genera una
   secuencia usando la cadena de Markov estimada y produce un CSV
   con la evolución acumulada de vocales/consonantes para graficar.

   Compilar: gcc -o cadenas_markov cadenas_markov.c
   Ejecutar: ./cadenas_markov [input_file] [output_csv] [gen_length]
   Ejemplo: ./cadenas_markov frases.txt markov_output.csv 500
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <time.h>

int is_vowel_char(char c)
{
    c = tolower((unsigned char)c);
    return (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u');
}

int main(int argc, char **argv)
{
    const char *input = "frases.txt";
    const char *outcsv = "markov_output.csv";
    int gen_length = 500;

    if (argc >= 2)
        input = argv[1];
    if (argc >= 3)
        outcsv = argv[2];
    if (argc >= 4)
        gen_length = atoi(argv[3]);
    int do_plot = 0;
    if (argc >= 5 && (strcmp(argv[4], "--plot") == 0 || strcmp(argv[4], "plot") == 0))
        do_plot = 1;

    FILE *f = fopen(input, "r");
    if (!f)
    {
        fprintf(stderr, "No se pudo abrir '%s'\n", input);
        return 1;
    }

    // Read file and build v/c sequence
    size_t cap = 1024;
    char *vcseq = malloc(cap);
    if (!vcseq)
        return 2;
    size_t len = 0;

    int ch;
    while ((ch = fgetc(f)) != EOF)
    {
        if (isalpha(ch) && (unsigned char)ch < 128)
        {
            char c = (char)ch;
            if (is_vowel_char(c))
            {
                if (len + 1 >= cap)
                {
                    cap *= 2;
                    vcseq = realloc(vcseq, cap);
                    if (!vcseq)
                        return 3;
                }
                vcseq[len++] = 'v';
            }
            else
            {
                if (len + 1 >= cap)
                {
                    cap *= 2;
                    vcseq = realloc(vcseq, cap);
                    if (!vcseq)
                        return 3;
                }
                vcseq[len++] = 'c';
            }
        }
        // ignore spaces, punctuation, non-ascii letters (simple handling)
    }
    fclose(f);

    if (len == 0)
    {
        fprintf(stderr, "No se encontraron letras ascii en '%s'\n", input);
        free(vcseq);
        return 1;
    }

    // Null-terminate for convenience
    if (len + 1 >= cap)
    {
        vcseq = realloc(vcseq, len + 1);
        if (!vcseq)
            return 4;
    }
    vcseq[len] = '\0';

    // Count bigrams
    long vv = 0, vc = 0, cv = 0, cc = 0;
    for (size_t i = 0; i + 1 < len; ++i)
    {
        char a = vcseq[i];
        char b = vcseq[i + 1];
        if (a == 'v' && b == 'v')
            ++vv;
        else if (a == 'v' && b == 'c')
            ++vc;
        else if (a == 'c' && b == 'v')
            ++cv;
        else if (a == 'c' && b == 'c')
            ++cc;
    }

    long total_pairs = vv + vc + cv + cc;
    printf("Secuencia vc (primeros 200): %.200s\n\n", vcseq);
    printf("Longitud (letras): %zu\n", len);
    printf("Bigrama counts:\n");
    printf("  vv: %ld\n  vc: %ld\n  cv: %ld\n  cc: %ld\n", vv, vc, cv, cc);
    printf("Total pares: %ld\n", total_pairs);

    if (total_pairs > 0)
    {
        printf("Probabilidades (frecuencia / total pares):\n");
        printf("  vv: %.6f\n", (double)vv / total_pairs);
        printf("  vc: %.6f\n", (double)vc / total_pairs);
        printf("  cv: %.6f\n", (double)cv / total_pairs);
        printf("  cc: %.6f\n", (double)cc / total_pairs);
    }

    // Transition probabilities P(next | current)
    double p_v_v = 0.5, p_v_c = 0.5, p_c_v = 0.5, p_c_c = 0.5;
    long v_total = vv + vc;
    long c_total = cv + cc;
    if (v_total > 0)
    {
        p_v_v = (double)vv / v_total;
        p_v_c = (double)vc / v_total;
    }
    if (c_total > 0)
    {
        p_c_v = (double)cv / c_total;
        p_c_c = (double)cc / c_total;
    }

    printf("\nTransiciones condicionadas (P(next|current)):\n");
    printf("  P(v->v)=%.4f  P(v->c)=%.4f\n", p_v_v, p_v_c);
    printf("  P(c->v)=%.4f  P(c->c)=%.4f\n", p_c_v, p_c_c);

    /* Calcular y mostrar la distribución estacionaria teórica de la cadena de Markov
    Para dos estados (v, c) con p_v_c = P(v->c) y p_c_v = P(c->v):
        pi_v = p_c_v / (p_c_v + p_v_c)
        pi_c = 1 - pi_v
    Esta distribución es el valor al que tiende la fracción acumulada a largo plazo. */
    double pi_v = 0.5, pi_c = 0.5;
    double denom = p_c_v + p_v_c;
    if (denom > 0.0)
    {
        pi_v = p_c_v / denom;
        pi_c = 1.0 - pi_v;
    }
    printf("\nDistribucion estacionaria teorica (P_lim):\n");
    printf("  pi_v (vocal) = %.6f\n", pi_v);
    printf("  pi_c (consonante) = %.6f\n", pi_c);
    // Seed RNG
    srand((unsigned int)time(NULL));

    // Choose initial symbol proportional to counts of v/c in data
    long count_v = 0, count_c = 0;
    for (size_t i = 0; i < len; ++i)
    {
        if (vcseq[i] == 'v')
            ++count_v;
        else
            ++count_c;
    }
    double p_start_v = 0.5;
    if (count_v + count_c > 0)
        p_start_v = (double)count_v / (count_v + count_c);

    char *gen = malloc(gen_length + 1);
    if (!gen)
    {
        free(vcseq);
        return 5;
    }
    // initial
    double r = (double)rand() / RAND_MAX;
    gen[0] = (r < p_start_v) ? 'v' : 'c';

    for (int i = 1; i < gen_length; ++i)
    {
        char prev = gen[i - 1];
        double r2 = (double)rand() / RAND_MAX;
        if (prev == 'v')
        {
            gen[i] = (r2 < p_v_v) ? 'v' : 'c';
        }
        else
        {
            gen[i] = (r2 < p_c_v) ? 'v' : 'c';
        }
    }
    gen[gen_length] = '\0';

    // Produce CSV with cumulative fractions to plot
    FILE *out = fopen(outcsv, "w");
    if (!out)
    {
        fprintf(stderr, "No se pudo crear '%s'\n", outcsv);
    }
    else
    {
        fprintf(out, "index,cumulative_v_fraction,cumulative_c_fraction,symbol\n");
        long cum_v = 0, cum_c = 0;
        for (int i = 0; i < gen_length; ++i)
        {
            if (gen[i] == 'v')
                ++cum_v;
            else
                ++cum_c;
            double fv = (double)cum_v / (i + 1);
            double fc = (double)cum_c / (i + 1);
            fprintf(out, "%d,%.6f,%.6f,%c\n", i + 1, fv, fc, gen[i]);
        }
        fclose(out);
        printf("\nArchivo CSV escrito: %s (para graficar la evolucion)\n", outcsv);
    }

    // Print generated sequence summary and first/last parts
    printf("\nGenerada (len=%d) primeras 200: %.200s\n", gen_length, gen);
    // Also show final vowel/consonant ratio
    long final_v = 0;
    for (int i = 0; i < gen_length; ++i)
        if (gen[i] == 'v')
            ++final_v;
    printf("Proporcion final v: %.4f  c: %.4f\n", (double)final_v / gen_length, 1.0 - (double)final_v / gen_length);

    // Cleanup
    free(vcseq);
    free(gen);

    return 0;
}
