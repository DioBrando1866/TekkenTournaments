// Registro de usuarios
document.addEventListener("DOMContentLoaded", () => {
  const users = JSON.parse(localStorage.getItem("users")) || [];
  const tournaments = JSON.parse(localStorage.getItem("tournaments")) || [];

  // Registro
  const registerForm = document.getElementById("register-form");
  if (registerForm) {
    registerForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const user = document.getElementById("reg-user").value;
      const pass = document.getElementById("reg-pass").value;
      if (users.find(u => u.user === user)) {
        alert("Ese usuario ya existe.");
        return;
      }
      users.push({ user, pass });
      localStorage.setItem("users", JSON.stringify(users));
      alert("Registro exitoso. ¡Ahora inicia sesión!");
      window.location.href = "login.html";
    });
  }

  // Login
  const loginForm = document.getElementById("login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const user = document.getElementById("login-user").value;
      const pass = document.getElementById("login-pass").value;
      const validUser = users.find(u => u.user === user && u.pass === pass);
      if (validUser) {
        localStorage.setItem("loggedUser", user);
        alert(`Bienvenido, ${user}!`);
        window.location.href = "tournaments.html";
      } else {
        alert("Usuario o contraseña incorrectos.");
      }
    });
  }

  // Crear Torneo
  const createForm = document.getElementById("create-tournament-form");
  if (createForm) {
    createForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const name = document.getElementById("tournament-name").value;
      tournaments.push({ name, players: [] });
      localStorage.setItem("tournaments", JSON.stringify(tournaments));
      alert("Torneo creado con éxito!");
      window.location.href = "tournaments.html";
    });
  }

  // Mostrar torneos
  const tournamentList = document.getElementById("tournament-list");
  if (tournamentList) {
    if (tournaments.length === 0) {
      tournamentList.innerHTML = "<p>No hay torneos aún.</p>";
    } else {
      tournaments.forEach(t => {
        const div = document.createElement("div");
        div.classList.add("tournament-card");
        div.innerHTML = `<h3>${t.name}</h3>
                         <p>Jugadores inscritos: ${t.players.length}</p>`;
        tournamentList.appendChild(div);
      });
    }
  }
});
