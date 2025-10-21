// Lista de torneos con sus jugadores inscritos
const tournaments = [
    { id: 1, name: 'Tekken 7', players: [] },
    { id: 2, name: 'Tekken Tag Tournament', players: [] },
    { id: 3, name: 'Tekken 3', players: [] },
];

// Función para mostrar los torneos disponibles
function displayTournaments() {
    const tournamentList = document.getElementById('tournament-list');

    tournamentList.innerHTML = '';
    tournaments.forEach(tournament => {
        const tournamentDiv = document.createElement('div');
        tournamentDiv.textContent = tournament.name;
        
        const playerList = document.createElement('ul');
        if (tournament.players.length > 0) {
            tournament.players.forEach(player => {
                const playerItem = document.createElement('li');
                playerItem.textContent = player;
                playerList.appendChild(playerItem);
            });
        } else {
            const noPlayers = document.createElement('p');
            noPlayers.textContent = 'No hay jugadores inscritos aún.';
            tournamentDiv.appendChild(noPlayers);
        }

        tournamentDiv.appendChild(playerList);
        tournamentList.appendChild(tournamentDiv);
    });
}

// Función para registrar un jugador
document.getElementById('register-form').addEventListener('submit', function(event) {
    event.preventDefault();
    const playerName = document.getElementById('player-name').value;
    if (playerName === '') {
        alert('Por favor, ingresa un nombre de usuario.');
        return;
    }
    alert(`¡Bienvenido, ${playerName}! Estás registrado con éxito.`);
    document.getElementById('player-name').value = '';
});

// Función para crear un nuevo torneo
document.getElementById('create-tournament-form').addEventListener('submit', function(event) {
    event.preventDefault();
    const tournamentName = document.getElementById('tournament-name-input').value;
    if (tournamentName === '') {
        alert('Por favor, ingresa un nombre para el torneo.');
        return;
    }

    const newTournament = {
        id: tournaments.length + 1,
        name: tournamentName,
        players: []
    };
    tournaments.push(newTournament);
    alert(`¡El torneo ${tournamentName} ha sido creado!`);
    document.getElementById('tournament-name-input').value = '';
    displayTournaments();
});

// Mostrar torneos al cargar la página
window.onload = displayTournaments;
