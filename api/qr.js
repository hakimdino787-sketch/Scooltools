export default async function handler(req,res){
  res.setHeader("X-Content-Type-Options","nosniff");
  if(req.method!=="GET")return res.status(405).json({error:"GET required"});
  const data=typeof req.query?.data==="string"?req.query.data:"";
  if(!data)return res.status(400).json({error:"Missing data"});
  if(data.length>4000)return res.status(400).json({error:"Data too long"});
  try{
    const upstream="https://api.qrserver.com/v1/create-qr-code/?size=240x240&data="+encodeURIComponent(data);
    const r=await fetch(upstream);
    if(!r.ok)throw new Error("QR provider error");
    const type=r.headers.get("content-type")||"image/png";
    const buf=Buffer.from(await r.arrayBuffer());
    res.setHeader("Content-Type",type);
    res.setHeader("Cache-Control","public, max-age=3600, s-maxage=3600");
    res.status(200).send(buf);
  }catch(e){res.status(502).json({error:"QR service unavailable"});}
}