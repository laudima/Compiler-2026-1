%{
/*
* SECCIÓN 1: DEFINICIONES
* Este bloque de código C se copia tal cual al inicio del archivo C generado.
* Es ideal para incluir librerías y declarar variables globales.
*/
#include <stdio.h>

%}

/* Aquí definimos "alias" para nuestras expresiones regulares. Facilita la lectura. */
DIGITO [0-9]
LETRA [a-zA-Z]
ID {LETRA}({LETRA}|{DIGITO})*
NUMERO {DIGITO}+(\.{DIGITO}+)?
OPERADOR [+\-*/]
OP_RELACIONAL [==, !=, <,>, <=, >=]

%%
{NUMERO} { printf("TOKEN TIPO NUMERO: '%s'\n", yytext); }
{ID} { printf("TOKEN TIPO IDENTIFICADOR: '%s'\n", yytext); }
{OPERADOR} { printf("TOKEN TIPO OPERADOR: '%s'\n", yytext); }
{OP_RELACIONAL} { printf("TOKEN TIPO OPERADOR RELACIONAL: '%s'\n", yytext);}


[ \t\n]+ { /* Regla para ignorar espacios, tabs y saltos de línea. No tiene acción. */ }

. { printf(">> Caracter desconocido: %s\n", yytext); }
%%


/*
* SECCIÓN 3: CÓDIGO DE USUARIO
* Código C adicional. Generalmente aquí se coloca la función main()
* que inicia el analizador.
*/
int main(int argc, char *argv[]) {
// **IMPORTANTE**: Modifica esta línea con tu nombre para el entregable.
printf("Practica realizada por: Laura Itzel Rodriguez Dimayuga\n\n");
printf("Iniciando analizador lexico. Ingresa texto (Ctrl+D para terminar):\n");
printf("------------------------------------------------------------------\n");
yylex(); // Llama a la función del analizador para que empiece a escanear.
printf("------------------------------------------------------------------\n");
printf("Análisis finalizado.\n");
return 0;
}

/* Flex requiere esta función. Para este ejemplo, simplemente retornamos 1. */
int yywrap() {
return 1;
}