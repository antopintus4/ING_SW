const CodiceFiscale = require('codice-fiscale-js');

try {
  const cf = new CodiceFiscale({
    name: 'Mario',
    surname: 'Rossi',
    gender: 'M',
    day: 1,
    month: 1,
    year: 1990,
    birthplace: 'Roma'
  });
  console.log('CF:', cf.code);
} catch(e) {
  console.log('Error:', e.message);
}
