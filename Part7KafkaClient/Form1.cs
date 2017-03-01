using KafkaNet;
using KafkaNet.Model;
using KafkaNet.Protocol;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace KafkaTestClient
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private static void AddFileToKafka(byte[] filename, byte[] message, string topic)
        {
            KafkaOptions options = new KafkaOptions(new Uri("http://sandbox.hortonworks.com:6667"));
            using (BrokerRouter router = new BrokerRouter(options))
            using (Producer client = new Producer(router))
            {
                var topicMetas = router.GetTopicMetadata(topic);

                var responses = client.SendMessageAsync(topic, 
                    new[] {
                        new KafkaNet.Protocol.Message
                        {
                            Key = filename,
                            Value = message
                        }});

                responses.Wait();
                if (responses != null)
                {
                    ProduceResponse response = responses.Result.FirstOrDefault();
                }
            }
            Console.WriteLine("File added");
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (openFileDialog1.ShowDialog() == DialogResult.OK)
            {
                string filecontent = File.ReadAllText(openFileDialog1.FileName);

                Form1.AddFileToKafka(
                    Encoding.Default.GetBytes(openFileDialog1.FileName),
                    Encoding.Default.GetBytes(filecontent), 
                    "stormwc");
            }
        }
    }
}
