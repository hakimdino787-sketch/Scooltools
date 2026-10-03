const ORIGIN="https://myscool.vercel.app";
export default function handler(req,res){
  res.setHeader("Access-Control-Allow-Origin",ORIGIN);
  res.setHeader("Access-Control-Allow-Methods","GET,OPTIONS");
  res.setHeader("Vary","Origin");
  if(req.method==="OPTIONS")return res.status(204).end();
  if(req.method!=="GET")return res.status(405).json({error:"GET required"});
  res.status(200).json({ok:true,service:"Scooltools API",version:"1.0.0",time:new Date().toISOString()});
}