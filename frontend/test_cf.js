const CodiceFiscale = require('codice-fiscale-js');
const cf = new CodiceFiscale({
    name: 'Antonio',
    surname: 'Pintus',
    gender: 'M',
    day: 1,
    month: 1,
    year: 2000,
    birthplace: 'Roma',
    birthplaceProvincia: 'RM'
});
console.log(cf.code);
