const ChangeUser = () => {
  if (process.env.NODE_ENV !== "development") {
    return null;
  }

  const getLink = (email: string, name: string) => (
    <div style={{ marginTop: "10px" }}>
      <a
        href={`/#access_token=SR-DEMO-LOGIN%20${email}`}
        style={{ padding: 0 }}
      >
        Login as {name}
      </a>
    </div>
  );

  const url = ``;
  return (
    <>
      {getLink("jamar@example.com", "Jamar (Entry Only)")}
      {getLink("ruby@example.com", "Ruby (Basic User)")}
      {getLink("sarah@example.com", "Sarah (Admin)")}
      {getLink("bob@example.com", "Bob (Super Admin)")}
    </>
  );
};

export default ChangeUser;
