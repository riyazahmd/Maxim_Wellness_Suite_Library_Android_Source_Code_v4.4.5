<?php
// Database configuration
$servername = "localhost";
$username = "hfbmbhmy_vaccine";
$password = "kcry2gb79ff250";
$dbname = "hfbmbhmy_vaccine";

// Create connection
$conn = new mysqli($servername, $username, $password);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Create database
$sql = "CREATE DATABASE IF NOT EXISTS $dbname";
if ($conn->query($sql) === TRUE) {
    echo "Database created successfully\n";
} else {
    echo "Error creating database: " . $conn->error;
}

// Select database
$conn->select_db($dbname);

// Create table for survey responses
$sql = "CREATE TABLE IF NOT EXISTS survey_responses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    age_group VARCHAR(50),
    gender VARCHAR(10),
    residence VARCHAR(10),
    education VARCHAR(50),
    income VARCHAR(50),
    vaccinations_received VARCHAR(50),
    reasons_for_missing_vaccinations VARCHAR(255),
    missed_or_delayed_vaccination VARCHAR(5),
    reason_for_missed_delayed VARCHAR(255),
    adverse_events VARCHAR(5),
    healthcare_facility_distance VARCHAR(50),
    healthcare_experience VARCHAR(50),
    consult_frequency VARCHAR(50),
    provider_explanation VARCHAR(50),
    knowledge_level VARCHAR(50),
    importance_of_immunization VARCHAR(50),
    vaccine_side_effects VARCHAR(50),
    trust_in_healthcare VARCHAR(50),
    discouragement_from_vaccination VARCHAR(5),
    reasons_for_discouragement VARCHAR(255),
    financial_impact VARCHAR(50),
    suggested_improvements VARCHAR(255),
    future_participation VARCHAR(5)
)";

if ($conn->query($sql) === TRUE) {
    echo "Table created successfully\n";
} else {
    echo "Error creating table: " . $conn->error;
}

$conn->close();
?>

<!DOCTYPE html>
<html>
<head>
    <title>Immunization and Health Questionnaire</title>
</head>
<body>
    <h2>Immunization and Health Questionnaire for Parents</h2>
    <form method="POST" action="save_survey.php">
        <h3>Section 1: Demographic Information</h3>
        <label>1. What is the age of your child?</label><br>
        <select name="age_group">
            <option value="Under 2 years">Under 2 years</option>
            <option value="2-4 years">2-4 years</option>
            <option value="5-7 years">5-7 years</option>
            <option value="8-10 years">8-10 years</option>
            <option value="11-12 years">11-12 years</option>
        </select><br><br>

        <label>2. What is the gender of your child?</label><br>
        <input type="radio" name="gender" value="Male"> Male<br>
        <input type="radio" name="gender" value="Female"> Female<br>
        <input type="radio" name="gender" value="Other"> Other<br><br>

        <label>3. What is your place of residence?</label><br>
        <input type="radio" name="residence" value="Urban"> Urban<br>
        <input type="radio" name="residence" value="Rural"> Rural<br><br>

        <label>4. What is your highest level of education?</label><br>
        <select name="education">
            <option value="No formal education">No formal education</option>
            <option value="Primary school">Primary school</option>
            <option value="Secondary school">Secondary school</option>
            <option value="Higher secondary">Higher secondary</option>
            <option value="Graduate">Graduate</option>
            <option value="Postgraduate or higher">Postgraduate or higher</option>
        </select><br><br>

        <label>5. What is your monthly household income?</label><br>
        <select name="income">
            <option value="Less than ₹10,000">Less than ₹10,000</option>
            <option value="₹10,000 - ₹20,000">₹10,000 - ₹20,000</option>
            <option value="₹20,000 - ₹50,000">₹20,000 - ₹50,000</option>
            <option value="₹50,000 - ₹1,00,000">₹50,000 - ₹1,00,000</option>
            <option value="More than ₹1,00,000">More than ₹1,00,000</option>
        </select><br><br>

        <label>6. Has your child received all the recommended vaccinations according to the national immunization schedule?</label><br>
        <input type="radio" name="vaccinations_received" value="Yes, all vaccinations"> Yes, all vaccinations<br>
        <input type="radio" name="vaccinations_received" value="Some vaccinations"> Some vaccinations<br>
        <input type="radio" name="vaccinations_received" value="No vaccinations"> No vaccinations<br><br>

        <label>7. If not all vaccinations were received, what were the reasons? (Select all that apply)</label><br>
        <input type="checkbox" name="reasons_for_missing_vaccinations[]" value="Lack of awareness about vaccination schedule"> Lack of awareness about vaccination schedule<br>
        <input type="checkbox" name="reasons_for_missing_vaccinations[]" value="Concerns about vaccine safety"> Concerns about vaccine safety<br>
        <input type="checkbox" name="reasons_for_missing_vaccinations[]" value="Vaccine availability issues"> Vaccine availability issues<br>
        <input type="checkbox" name="reasons_for_missing_vaccinations[]" value="Religious or cultural beliefs"> Religious or cultural beliefs<br>
        <input type="checkbox" name="reasons_for_missing_vaccinations[]" value="Child was unwell at the time"> Child was unwell at the time<br>
        <input type="checkbox" name="reasons_for_missing_vaccinations[]" value="Other"> Other<br><br>

        <label>8. Have you ever missed or delayed a vaccination for your child?</label><br>
        <input type="radio" name="missed_or_delayed_vaccination" value="Yes"> Yes<br>
        <input type="radio" name="missed_or_delayed_vaccination" value="No"> No<br><br>

        <label>9. If you missed or delayed, what was the reason? (Select all that apply)</label><br>
        <input type="checkbox" name="reason_for_missed_delayed[]" value="Forgot the schedule"> Forgot the schedule<br>
        <input type="checkbox" name="reason_for_missed_delayed[]" value="Long waiting times at healthcare facilities"> Long waiting times at healthcare facilities<br>
        <input type="checkbox" name="reason_for_missed_delayed[]" value="Lack of transport to the healthcare facility"> Lack of transport to the healthcare facility<br>
        <input type="checkbox" name="reason_for_missed_delayed[]" value="Child's illness at the time of vaccination"> Child's illness at the time of vaccination<br>
        <input type="checkbox" name="reason_for_missed_delayed[]" value="Fear of side effects"> Fear of side effects<br>
        <input type="checkbox" name="reason_for_missed_delayed[]" value="Other"> Other<br><br>

        <label>10. Have you or any member of your family ever experienced adverse events following immunization that influenced your decision not to vaccinate your child?</label><br>
        <input type="radio" name="adverse_events" value="Yes"> Yes<br>
        <input type="radio" name="adverse_events" value="No"> No<br><br>

        <label>11. How far is the nearest healthcare facility from your home?</label><br>
        <select name="healthcare_facility_distance">
            <option value="Less than 1 km">Less than 1 km</option>
            <option value="1-5 km">1-5 km</option>
            <option value="5-10 km">5-10 km</option>
            <option value="More than 10 km">More than 10 km</option>
        </select><br><br>

        <label>12. How would you rate your experience with healthcare services regarding immunization?</label><br>
        <select name="healthcare_experience">
            <option value="Very satisfied">Very satisfied</option>
            <option value="Satisfied">Satisfied</option>
            <option value="Neutral">Neutral</option>
            <option value="Dissatisfied">Dissatisfied</option>
            <option value="Very dissatisfied">Very dissatisfied</option>
        </select><br><br>

        <label>13. How often do you consult healthcare providers regarding your child's vaccination?</label><br>
        <select name="consult_frequency">
            <option value="Always">Always</option>
            <option value="Often">Often</option>
            <option value="Sometimes">Sometimes</option>
            <option value="Rarely">Rarely</option>
            <option value="Never">Never</option>
        </select><br><br>

        <label>14. Do you feel healthcare providers adequately explain the benefits and risks of vaccinations?</label><br>
        <input type="radio" name="provider_explanation" value="Yes, always"> Yes, always<br>
        <input type="radio" name="provider_explanation" value="Yes, sometimes"> Yes, sometimes<br>
        <input type="radio" name="provider_explanation" value="No, rarely"> No, rarely<br>
        <input type="radio" name="provider_explanation" value="No, never"> No, never<br><br>

        <label>15. How knowledgeable do you feel about the vaccines your child should receive?</label><br>
        <select name="knowledge_level">
            <option value="Very knowledgeable">Very knowledgeable</option>
            <option value="Somewhat knowledgeable">Somewhat knowledgeable</option>
            <option value="Not very knowledgeable">Not very knowledgeable</option>
            <option value="Not knowledgeable at all">Not knowledgeable at all</option>
        </select><br><br>

        <label>16. How important do you believe immunizations are for your child's health?</label><br>
        <select name="importance_of_immunization">
            <option value="Very important">Very important</option>
            <option value="Important">Important</option>
            <option value="Neutral">Neutral</option>
            <option value="Not very important">Not very important</option>
            <option value="Not important at all">Not important at all</option>
        </select><br><br>

        <label>17. Do you think vaccinations have any significant side effects?</label><br>
        <select name="vaccine_side_effects">
            <option value="Yes, many side effects">Yes, many side effects</option>
            <option value="Yes, some side effects">Yes, some side effects</option>
            <option value="No, few side effects">No, few side effects</option>
            <option value="No, no side effects">No, no side effects</option>
        </select><br><br>

        <label>18. Do you trust the information provided by healthcare professionals about vaccines?</label><br>
        <input type="radio" name="trust_in_healthcare" value="Yes, completely"> Yes, completely<br>
        <input type="radio" name="trust_in_healthcare" value="Yes, to some extent"> Yes, to some extent<br>
        <input type="radio" name="trust_in_healthcare" value="No, not much"> No, not much<br>
        <input type="radio" name="trust_in_healthcare" value="No, not at all"> No, not at all<br><br>

        <label>19. Does anyone in your family or community discourage you from vaccinating your child?</label><br>
        <input type="radio" name="discouragement_from_vaccination" value="Yes"> Yes<br>
        <input type="radio" name="discouragement_from_vaccination" value="No"> No<br><br>

        <label>20. If yes, what is the reason for discouragement? (Select all that apply)</label><br>
        <input type="checkbox" name="reasons_for_discouragement[]" value="Cultural beliefs"> Cultural beliefs<br>
        <input type="checkbox" name="reasons_for_discouragement[]" value="Religious beliefs"> Religious beliefs<br>
        <input type="checkbox" name="reasons_for_discouragement[]" value="Misinformation about vaccines"> Misinformation about vaccines<br>
        <input type="checkbox" name="reasons_for_discouragement[]" value="Past experiences"> Past experiences<br>
        <input type="checkbox" name="reasons_for_discouragement[]" value="Other"> Other<br><br>

        <label>21. Do you feel that your financial situation impacts your ability to get your child vaccinated?</label><br>
        <select name="financial_impact">
            <option value="Yes, significantly">Yes, significantly</option>
            <option value="Yes, somewhat">Yes, somewhat</option>
            <option value="No, not at all">No, not at all</option>
        </select><br><br>

        <label>22. What do you think could be improved in the vaccination process? (Select all that apply)</label><br>
        <input type="checkbox" name="suggested_improvements[]" value="Better awareness campaigns"> Better awareness campaigns<br>
        <input type="checkbox" name="suggested_improvements[]" value="Improved healthcare services"> Improved healthcare services<br>
        <input type="checkbox" name="suggested_improvements[]" value="Reduced waiting times"> Reduced waiting times<br>
        <input type="checkbox" name="suggested_improvements[]" value="More information about vaccine safety"> More information about vaccine safety<br>
        <input type="checkbox" name="suggested_improvements[]" value="Easier access to healthcare facilities"> Easier access to healthcare facilities<br>
        <input type="checkbox" name="suggested_improvements[]" value="Other"> Other<br><br>

        <label>23. Would you be willing to participate in future health-related surveys or programs?</label><br>
        <input type="radio" name="future_participation" value="Yes"> Yes<br>
        <input type="radio" name="future_participation" value="No"> No<br>
        <input type="radio" name="future_participation" value="Maybe"> Maybe<br><br>

        <input type="submit" value="Submit">
    </form>
</body>
</html>

<?php
// save_survey.php file to handle form submission and save data to database
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Create connection
    $conn = new mysqli($servername, $username, $password, $dbname);

    // Check connection
    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }

    // Prepare and bind
    $stmt = $conn->prepare("INSERT INTO survey_responses (
        age_group, gender, residence, education, income, vaccinations_received, reasons_for_missing_vaccinations, 
        missed_or_delayed_vaccination, reason_for_missed_delayed, adverse_events, healthcare_facility_distance, 
        healthcare_experience, consult_frequency, provider_explanation, knowledge_level, importance_of_immunization, 
        vaccine_side_effects, trust_in_healthcare, discouragement_from_vaccination, reasons_for_discouragement, 
        financial_impact, suggested_improvements, future_participation
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    $stmt->bind_param(
        "sssssssssssssssssssssss",
        $age_group, $gender, $residence, $education, $income, $vaccinations_received, 
        $reasons_for_missing_vaccinations, $missed_or_delayed_vaccination, $reason_for_missed_delayed, 
        $adverse_events, $healthcare_facility_distance, $healthcare_experience, $consult_frequency, 
        $provider_explanation, $knowledge_level, $importance_of_immunization, $vaccine_side_effects, 
        $trust_in_healthcare, $discouragement_from_vaccination, $reasons_for_discouragement, 
        $financial_impact, $suggested_improvements, $future_participation
    );

    // Set parameters and execute
    $age_group = $_POST['age_group'];
    $gender = $_POST['gender'];
    $residence = $_POST['residence'];
    $education = $_POST['education'];
    $income = $_POST['income'];
    $vaccinations_received = $_POST['vaccinations_received'];
    $reasons_for_missing_vaccinations = implode(", ", $_POST['reasons_for_missing_vaccinations'] ?? []);
    $missed_or_delayed_vaccination = $_POST['missed_or_delayed_vaccination'];
    $reason_for_missed_delayed = implode(", ", $_POST['reason_for_missed_delayed'] ?? []);
    $adverse_events = $_POST['adverse_events'];
    $healthcare_facility_distance = $_POST['healthcare_facility_distance'];
    $healthcare_experience = $_POST['healthcare_experience'];
    $consult_frequency = $_POST['consult_frequency'];
    $provider_explanation = $_POST['provider_explanation'];
    $knowledge_level = $_POST['knowledge_level'];
    $importance_of_immunization = $_POST['importance_of_immunization'];
    $vaccine_side_effects = $_POST['vaccine_side_effects'];
    $trust_in_healthcare = $_POST['trust_in_healthcare'];
    $discouragement_from_vaccination = $_POST['discouragement_from_vaccination'];
    $reasons_for_discouragement = implode(", ", $_POST['reasons_for_discouragement'] ?? []);
    $financial_impact = $_POST['financial_impact'];
    $suggested_improvements = implode(", ", $_POST['suggested_improvements'] ?? []);
    $future_participation = $_POST['future_participation'];

    if ($stmt->execute()) {
        echo "New record created successfully";
    } else {
        echo "Error: " . $stmt->error;
    }

    $stmt->close();
    $conn->close();
}
?>
