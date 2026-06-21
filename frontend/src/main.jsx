import React, { useState } from "react";
import ReactDOM from "react-dom/client";
import "./styles.css";

const API_URL = "http://localhost:8080";

function App() {
  const [profileId, setProfileId] = useState(null);
  const [message, setMessage] = useState("Ready");
  const [result, setResult] = useState(null);
  const [ocrFields, setOcrFields] = useState({});

  const [form, setForm] = useState({
    fullName: "Gowtham Boddu",
    email: "gowtham@example.com",
    panNumber: "ABCDE1234F",
    city: "Bengaluru",
    annualIncome: 1200000,
    standardDeduction: 50000,
    section80C: 120000,
    hraExemption: 80000,
    tdsPaid: 90000,
    regime: "OLD"
  });

  function handleChange(event) {
    const name = event.target.name;
    const value = event.target.value;
    const numberFields = ["annualIncome", "standardDeduction", "section80C", "hraExemption", "tdsPaid"];

    setForm({
      ...form,
      [name]: numberFields.includes(name) ? Number(value) : value
    });
  }

  async function saveProfile() {
    const response = await fetch(API_URL + "/api/profiles", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        id: profileId,
        fullName: form.fullName,
        email: form.email,
        panNumber: form.panNumber,
        city: form.city
      })
    });

    const profile = await response.json();
    setProfileId(profile.id);
    setMessage("Profile saved");
    return profile.id;
  }

  async function calculateTax() {
    const id = profileId || await saveProfile();

    const response = await fetch(API_URL + "/api/tax/calculate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        ...form,
        profileId: id
      })
    });

    const data = await response.json();
    setResult(data);
    setMessage("Tax calculated");
  }

  async function uploadDocument(event) {
    const file = event.target.files[0];
    if (!file) {
      return;
    }

    const data = new FormData();
    data.append("file", file);
    setMessage("Reading document...");

    const response = await fetch(API_URL + "/api/documents/upload", {
      method: "POST",
      body: data
    });

    const extracted = await response.json();
    setOcrFields(extracted.rawFields || {});
    setForm({
      ...form,
      annualIncome: Number(extracted.annualIncome || form.annualIncome),
      section80C: Number(extracted.section80C || form.section80C),
      hraExemption: Number(extracted.hraExemption || form.hraExemption),
      tdsPaid: Number(extracted.tdsPaid || form.tdsPaid)
    });
    setMessage("Document values added");
  }

  function rupees(value) {
    return "Rs. " + Number(value || 0).toLocaleString("en-IN");
  }

  return (
    <div className="page">
      <h1>SmartTax AI</h1>
      <p className="subtitle">Basic tax calculator with document upload and recommendations.</p>
      <p className="status">{message}</p>

      <div className="box">
        <h2>User Details</h2>
        <div className="form-grid">
          <label>Full Name<input name="fullName" value={form.fullName} onChange={handleChange} /></label>
          <label>Email<input name="email" value={form.email} onChange={handleChange} /></label>
          <label>PAN<input name="panNumber" value={form.panNumber} onChange={handleChange} /></label>
          <label>City<input name="city" value={form.city} onChange={handleChange} /></label>
        </div>
        <button onClick={saveProfile}>Save Profile</button>
      </div>

      <div className="box">
        <h2>Tax Details</h2>
        <div className="form-grid">
          <label>Annual Income<input type="number" name="annualIncome" value={form.annualIncome} onChange={handleChange} /></label>
          <label>Standard Deduction<input type="number" name="standardDeduction" value={form.standardDeduction} onChange={handleChange} /></label>
          <label>Section 80C<input type="number" name="section80C" value={form.section80C} onChange={handleChange} /></label>
          <label>HRA Exemption<input type="number" name="hraExemption" value={form.hraExemption} onChange={handleChange} /></label>
          <label>TDS Paid<input type="number" name="tdsPaid" value={form.tdsPaid} onChange={handleChange} /></label>
          <label>Tax Regime
            <select name="regime" value={form.regime} onChange={handleChange}>
              <option value="OLD">Old Regime</option>
              <option value="NEW">New Regime</option>
            </select>
          </label>
        </div>
        <button onClick={calculateTax}>Calculate Tax</button>
      </div>

      <div className="box">
        <h2>Upload Form 16 / Salary Slip</h2>
        <input type="file" accept=".pdf,.png,.jpg,.jpeg,.txt" onChange={uploadDocument} />
        <ul>
          {Object.keys(ocrFields).map((key) => (
            <li key={key}>{key}: {ocrFields[key]}</li>
          ))}
        </ul>
      </div>

      <div className="box">
        <h2>Result</h2>
        <div className="result-grid">
          <p><b>Taxable Income:</b> {rupees(result && result.taxableIncome)}</p>
          <p><b>Total Tax:</b> {rupees(result && result.totalTax)}</p>
          <p><b>Refund / Payable:</b> {rupees(result && result.refundOrPayable)}</p>
          <p><b>Regime:</b> {result ? result.regime : form.regime}</p>
        </div>

        <h3>Recommendations</h3>
        <ul>
          {result && result.recommendations
            ? result.recommendations.map((item) => <li key={item}>{item}</li>)
            : <li>Calculate tax to see recommendations.</li>}
        </ul>
      </div>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
