# Pruebas manuales de aceptación

Estas comprobaciones requieren Firebase configurado. No están marcadas como realizadas.

- [ ] Acceso: logo, selector en español; registro por correo, inicio, contraseña errónea y recuperación por correo real.
- [ ] Google: elegir cuenta, cancelar y volver a intentar. Si se configuró Meta, repetir con Facebook.
- [ ] Cerrar y abrir la app: conserva la cuenta y muestra directamente la lista.
- [ ] Registrar IoT / 2026-01-01 / 2 h / Teoría; IoT / 2026-01-02 / 1.5 h / Práctica; Redes / 2026-02-01 / 4 h / Laboratorio.
- [ ] Lista: aparecen tres sesiones con sus cuatro campos. Reiniciar la app y comprobar persistencia.
- [ ] Editar la sesión de 1.5 a 3 horas. Comprobar cambio inmediato y después de reiniciar.
- [ ] Abrir la misma cuenta en otro dispositivo y comprobar cambios en tiempo real en lista y gráficos.
- [ ] Gráficos sin filtro: enero 5 h, febrero 4 h; torta 2/9 Teoría, 3/9 Práctica y 4/9 Laboratorio.
- [ ] Filtrar IoT: enero 5 h; torta 40 % Teoría, 60 % Práctica y 0 % Laboratorio. Filtrar Redes y volver a todos.
- [ ] Agregar una sesión de enero de otro año y comprobar que tiene otra barra.
- [ ] Rechazar curso vacío, horas vacías, 0, negativas, >24 y fecha inexistente; permitir 0.25 y 24 horas.
- [ ] Cancelar una edición no modifica Firestore. Eliminar pide confirmación y actualiza ambos gráficos.
- [ ] Cerrar sesión A y entrar con B: no aparecen sesiones de A ni sus cursos en el filtro.
- [ ] En Rules Playground o emulador de Firestore: usuario sin autenticar no lee/escribe; B no puede leer/escribir/eliminar en users/A/sessions; A puede operar sus sesiones; campos extra, actividad inválida y horas fuera de rango se rechazan.
- [ ] Desactivar red: la app informa fallos; reconectar y comprobar sincronización. Evitar múltiples envíos mientras se guarda.
- [ ] Rotar el dispositivo, usar botón Atrás y teclado, y probar pantalla pequeña sin controles tapados.
