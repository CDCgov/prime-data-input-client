export const PATIENT_TERM = "person";
export const PATIENT_TERM_CAP = "Person";
export const PATIENT_TERM_PLURAL = "people";
export const PATIENT_TERM_PLURAL_CAP = "People";

// NOTE: Any time SimpleReport goes live in a new state, this file must be updated.
// Otherwise, organizations will not be able to create facilities in the new state.
export const liveJurisdictions = [
  "AL",
  "AZ",
  "CA",
  "CO",
  "FL",
  "GU",
  "LA",
  "MA",
  "MS",
  "MT",
  "ND",
  "NM",
  "OH",
  "PA",
  "TX",
  "VT",
];

// States which do not require a valid CLIA number for a facility
export const noCLIAValidationStates: (keyof typeof states)[] = ["CO"];

export const orderingProviderNotRequiredStates = ["ND"];

export const states = {
  AK: "Alaska",
  AL: "Alabama",
  AR: "Arkansas",
  AS: "American Samoa",
  AZ: "Arizona",
  CA: "California",
  CO: "Colorado",
  CT: "Connecticut",
  DC: "Washington, D.C.",
  DE: "Delaware",
  FL: "Florida",
  FM: "Micronesia",
  GA: "Georgia",
  GU: "Guam",
  HI: "Hawaii",
  IA: "Iowa",
  ID: "Idaho",
  IL: "Illinois",
  IN: "Indiana",
  KS: "Kansas",
  KY: "Kentucky",
  LA: "Louisiana",
  MA: "Massachusetts",
  MD: "Maryland",
  ME: "Maine",
  MH: "Marshall Islands",
  MI: "Michigan",
  MN: "Minnesota",
  MO: "Montana",
  MP: "Mariana Islands",
  MS: "Mississippi",
  MT: "Montana",
  NC: "North Carolina",
  ND: "North Dakota",
  NE: "Nebraska",
  NH: "New Hampshire",
  NJ: "New Jersey",
  NM: "New Mexico",
  NV: "Nevada",
  NY: "New York",
  OH: "Ohio",
  OK: "Oklahoma",
  OR: "Oregon",
  PA: "Pennsylvania",
  PR: "Puerto Rico",
  PW: "Palau",
  RI: "Rhode Island",
  SC: "South Carolina",
  SD: "South Dakota",
  TN: "Tennessee",
  TX: "Texas",
  UT: "Utah",
  VA: "Virginia",
  VI: "U.S. Virgin Islands",
  VT: "Vermont",
  WA: "Washington",
  WI: "Wisconsin",
  WV: "West Virginia",
  WY: "Wyoming",
};

export const stateCodes = Object.keys(states);

export const languages: Language[] = [
  "English",
  "Spanish",
  "Unknown",
  "Afrikaans",
  "Amaric",
  "American Sign Language",
  "Arabic",
  "Armenian",
  "Aromanian; Arumanian; Macedo-Romanian",
  "Bantu (other)",
  "Bengali",
  "Braile",
  "Burmese",
  "Cambodian",
  "Cantonese",
  "Caucasian (other)",
  "Chaochow",
  "Cherokee",
  "Chinese",
  "Creoles and pidgins, French-based (Other)",
  "Cushitic (other)",
  "Dakota",
  "Farsi",
  "Fiji",
  "Filipino; Pilipino",
  "French",
  "German",
  "Gujarati",
  "Hebrew",
  "Hindi",
  "Hmong",
  "Indonesian",
  "Italian",
  "Japanese",
  "Kannada",
  "Korean",
  "Kru languages",
  "Kurdish",
  "Laotian",
  "Latin",
  "Luganda",
  "Malayalam",
  "Mandar",
  "Mandarin",
  "Marathi",
  "Marshallese",
  "Mien",
  "Mixteca",
  "Mon-Khmer (Other)",
  "Mongolian",
  "Morrocan Arabic",
  "Navajo",
  "Nepali",
  "Not Specified",
  "Oaxacan",
  "Other",
  "Pashto",
  "Portuguese",
  "Punjabi",
  "Rarotongan; Cook Islands Maori",
  "Russian",
  "Samoan",
  "Sebuano",
  "Serbo Croatian",
  "Sign Languages",
  "Singhalese",
  "Somali",
  "Swahili",
  "Syrian",
  "Tagalog",
  "Tahitian",
  "Taiwanese",
  "Tamil",
  "Tegulu",
  "Thai",
  "Tigrinya",
  "Triqui",
  "Ukrainian",
  "Urdu",
  "Vietnamese",
  "Yiddish",
  "Zapotec",
];

export const urls = {
  FACILITY_INFO:
    process.env.REACT_APP_BASE_URL +
    "resources/using-simplereport/manage-facility-info/find-supported-jurisdictions/",
};

export const securityQuestions = [
  "What’s the first name of your best friend from high school?",
  "What was the first name of your favorite childhood friend?",
  "In what city or town was your first job?",
  "What’s the last name of your first boss?",
  "What’s your grandmother’s first name?",
  "What’s your oldest sibling’s middle name?",
  "What was the name of the street where you were living when you were 10 years old?",
  "What was the name of the street where you were living when you were in third grade?",
  "In what city or town did your parents meet?",
  "What’s the first name of your eldest cousin on your mother’s side?",
  "What’s the last name of your best friend?",
  "What was the make and model of your first car?",
  "What was the name of the company where you had your first job?",
  "What’s the first name of your oldest nephew?",
  "What was the first name of the first person you dated?",
  "What was the first name of your first boyfriend or girlfriend?",
];

export const accountCreationSteps = [
  { label: "Create your password", value: "0", order: 0 },
  { label: "Select your security question", value: "1", order: 1 },
  { label: "Set up authentication", value: "2", order: 2 },
];

export const facilitySample = {
  cliaNumber: "dasdasd",
  phone: "20321321",
  email: null,
  deviceTypes: [],
  defaultDevice: "asdsad",
  street: " ",
  streetTwo: " ",
  city: " ",
  state: " ",
  zipCode: " ",
  orderingProvider: {
    firstName: "Any prov",
    middleName: " ",
    lastName: " ",
    suffix: " ",
    NPI: " ",
    phone: " ",
    street: " ",
    streetTwo: " ",
    city: " ",
    state: " ",
    zipCode: " ",
  },
};
export const patientSample:Patient = {
  firstName: "Jhon",
  middleName: "Sample",
  lastName: "Doe",
  internalId: "",
  testResultDelivery: "",
  facilityId: null,
  residentCongregateSetting: true,
  employedInHealthcare: true,
  birthDate: new Date("2020-02-02"),
  lookupId: "",
  role: 'STAFF',
  race: 'refused',
  ethnicity: "hispanic",
  gender: "female",
  tribalAffiliation: "46",
  telephone: "",
  phoneNumbers:  null,
  county: "",
  email: "",
  preferredLanguage: null,
  street: "",
  streetTwo:  null,
  city:   null,
  state: "",
  zipCode: "",
}
