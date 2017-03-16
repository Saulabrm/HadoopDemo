using Storm.DRPC;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StormVideoClient
{
    class Program
    {
        static void Main(string[] args)
        {
            var bytes = File.ReadAllBytes(@"c:\out.mp4");
            string base64Str = Convert.ToBase64String(bytes);

            DRPCClient client = new DRPCClient("localhost", 3772);
            string result = client.execute("video", base64Str);

            File.WriteAllBytes("c:\outmodified.mp4", Convert.FromBase64String(result));
        }
    }
}
